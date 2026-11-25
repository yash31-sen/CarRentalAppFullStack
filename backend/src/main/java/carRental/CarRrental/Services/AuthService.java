package carRental.CarRrental.Services;

import carRental.CarRrental.Dtos.*;
import carRental.CarRrental.Models.*;
import carRental.CarRrental.Repositories.AppUserRepository;
import carRental.CarRrental.Repositories.HostProfileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    private final JwtService jwtService;
    private final AppUserRepository userRepository;
    private final HostProfileRepository hostProfileRepository;
    private final TokenService tokenService;
    private final EmailService emailService;
    private final SmsProvider smsProvider;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public AuthService(
            AppUserRepository userRepository,
            HostProfileRepository hostProfileRepository,
            TokenService tokenService,
            EmailService emailService,
            SmsProvider smsProvider,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.hostProfileRepository = hostProfileRepository;
        this.tokenService = tokenService;
        this.emailService = emailService;
        this.smsProvider = smsProvider;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public void register(RegisterRequest req) {
        String email = req.getEmail().trim().toLowerCase();
        String intent = req.getRegistrationIntent() == null
                ? "USER"
                : req.getRegistrationIntent().trim().toUpperCase();

        var existingOpt = userRepository.findByEmail(email);
        if (existingOpt.isPresent()) {
            if ("HOST_APPLICATION".equals(intent)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "An account with this email already exists. Please log in to continue Host onboarding."
                );
            }
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This email is already registered. Please login or use a different email."
            );
        }

        if ("HOST_APPLICATION".equals(intent) && !Boolean.TRUE.equals(req.getTermsAccepted())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "You must accept the Host Terms & Conditions to apply as a Host."
            );
        }

        String mobile = req.getMobileNumber() != null && !req.getMobileNumber().isBlank()
                ? req.getMobileNumber().trim()
                : null;

        AppUser user = AppUser.builder()
                .name(req.getName().trim())
                .email(email)
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role(UserRole.USER) // Public registration ALWAYS creates USER only
                .mobileNumber(mobile)
                .mobileVerified(false)
                .termsAccepted(Boolean.TRUE.equals(req.getTermsAccepted()))
                .tokenVersion(0)
                .emailVerified(false)
                .isActive(true)
                .suspended(false)
                .build();
        user.getRoles().add(UserRole.USER);

        AppUser savedUser = userRepository.save(user);

        if ("HOST_APPLICATION".equals(intent)) {
            HostProfile draftProfile = HostProfile.builder()
                    .user(savedUser)
                    .phone(mobile != null ? mobile : "")
                    .city("PENDING")
                    .verificationStatus(HostVerificationStatus.PENDING_EMAIL_VERIFICATION)
                    .termsAccepted(true)
                    .active(false)
                    .build();
            hostProfileRepository.save(draftProfile);
        }

        sendVerificationEmailInternal(savedUser);
    }

    @Transactional
    public void resendVerificationEmail(String emailRaw) {
        if (emailRaw == null || emailRaw.isBlank()) {
            return;
        }
        String email = emailRaw.trim().toLowerCase();
        userRepository.findByEmail(email).ifPresent(user -> {
            if (!user.isEmailVerified() && user.isActive() && !user.isSuspendedFlag()) {
                sendVerificationEmailInternal(user);
            }
        });
    }

    public AuthResponse login(LoginRequest req) {
        String email = req.getEmail().trim().toLowerCase();

        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!user.isEmailVerified()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Please verify your email first");
        }

        if (!user.isActive() || user.isSuspendedFlag()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is deactivated or suspended");
        }

        boolean ok = passwordEncoder.matches(req.getPassword(), user.getPasswordHash());
        if (!ok) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String jwt = jwtService.generateToken(user);
        TokenService.GeneratedToken refreshToken = tokenService.createRefreshToken(user);

        return buildAuthResponse(user, jwt, refreshToken.rawToken());
    }

    @SuppressWarnings("unchecked")
    public AuthResponse googleLogin(GoogleLoginRequest req) {
        String idToken = req.getCredential();
        if (idToken == null || idToken.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Google ID Token is missing");
        }

        String googleUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> body;
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(googleUrl, Map.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Google token");
            }
            body = (Map<String, Object>) response.getBody();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Failed to verify Google token: " + e.getMessage());
        }

        String email = (String) body.get("email");
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email not provided by Google token");
        }
        email = email.trim().toLowerCase();

        String name = (String) body.get("name");
        if (name == null || name.trim().isEmpty()) {
            name = email.split("@")[0];
        }

        AppUser user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            user = AppUser.builder()
                    .name(name)
                    .email(email)
                    .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .role(UserRole.USER)
                    .emailVerified(true)
                    .isActive(true)
                    .suspended(false)
                    .tokenVersion(0)
                    .build();
            user.getRoles().add(UserRole.USER);
            user = userRepository.save(user);
        } else {
            if (!user.isEmailVerified()) {
                user.setEmailVerified(true);
                user = userRepository.save(user);
            }
            if (!user.isActive() || user.isSuspendedFlag()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User account is inactive or suspended");
            }
        }

        String jwt = jwtService.generateToken(user);
        TokenService.GeneratedToken refreshToken = tokenService.createRefreshToken(user);
        return buildAuthResponse(user, jwt, refreshToken.rawToken());
    }

    @Transactional
    public AuthResponse verifyEmail(String rawToken) {
        UserToken t = tokenService.findByRawTokenAndType(rawToken, TokenType.VERIFY_EMAIL)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid verification token"));

        if (t.isRevoked()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Verification link was revoked. Please use the latest link.");
        }
        if (t.isUsed()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Verification token already used");
        }
        if (t.isExpired()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Verification token expired");
        }

        AppUser user = t.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        hostProfileRepository.findByUser_Id(user.getId()).ifPresent(profile -> {
            if (profile.getVerificationStatus() == HostVerificationStatus.PENDING_EMAIL_VERIFICATION) {
                profile.setVerificationStatus(HostVerificationStatus.PENDING_DOCUMENTS);
                hostProfileRepository.save(profile);
            }
        });

        tokenService.markUsed(t);

        String jwt = jwtService.generateToken(user);
        TokenService.GeneratedToken refreshToken = tokenService.createRefreshToken(user);
        return buildAuthResponse(user, jwt, refreshToken.rawToken());
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest req) {
        String rawToken = req.getRefreshToken();
        if (rawToken == null || rawToken.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Refresh token is missing");
        }

        UserToken token = tokenService.findByRawTokenAndType(rawToken, TokenType.REFRESH_TOKEN)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (token.isRevoked()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token has been revoked");
        }
        if (token.isUsed()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token has already been used");
        }
        if (token.isExpired()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token has expired");
        }

        tokenService.markUsed(token);

        AppUser user = token.getUser();
        if (!user.isActive() || user.isSuspendedFlag()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User account is inactive or suspended");
        }

        String newJwt = jwtService.generateToken(user);
        TokenService.GeneratedToken newRefreshToken = tokenService.createRefreshToken(user);

        return buildAuthResponse(user, newJwt, newRefreshToken.rawToken());
    }

    @Transactional
    public void logout(String email) {
        if (email == null || email.isBlank()) {
            return;
        }
        userRepository.findByEmail(email.trim().toLowerCase()).ifPresent(user -> {
            user.incrementTokenVersion();
            userRepository.save(user);
            tokenService.revokeActiveTokens(user, TokenType.REFRESH_TOKEN);
        });
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest req) {
        if (req.getEmail() == null || req.getEmail().isBlank()) {
            return;
        }
        String email = req.getEmail().trim().toLowerCase();
        userRepository.findByEmail(email).ifPresent(user -> {
            TokenService.GeneratedToken resetToken = tokenService.createPasswordResetToken(user);
            String link = frontendUrl + "/reset-password?token=" + resetToken.rawToken();
            emailService.sendEmail(
                    user.getEmail(),
                    "Reset your password",
                    "Click to reset password (valid 15 minutes):\n" + link
            );
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        UserToken t = tokenService.findByRawTokenAndType(req.getToken(), TokenType.RESET_PASSWORD)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid token"));
        if (t.isRevoked()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token revoked");
        }
        if (t.isUsed()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token already used");
        }
        if (t.isExpired()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token expired");
        }

        AppUser user = t.getUser();
        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        user.incrementTokenVersion();
        userRepository.save(user);

        tokenService.markUsed(t);
        tokenService.revokeActiveTokens(user, TokenType.REFRESH_TOKEN);
    }

    @Transactional
    public void setupAccount(ResetPasswordRequest req) {
        UserToken t = tokenService.findByRawTokenAndType(req.getToken(), TokenType.ACCOUNT_SETUP)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid token"));
        if (t.isRevoked()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token revoked");
        }
        if (t.isUsed()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token already used");
        }
        if (t.isExpired()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token expired");
        }

        AppUser user = t.getUser();
        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        user.setEmailVerified(true);
        user.setActive(true);
        user.incrementTokenVersion();
        userRepository.save(user);

        tokenService.markUsed(t);
    }

    @Transactional
    public Map<String, Object> sendMobileVerification(String email, String mobileNumber) {
        AppUser user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (mobileNumber == null || mobileNumber.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mobile number is required");
        }

        String normalizedPhone = mobileNumber.trim();
        if (!normalizedPhone.equals(user.getMobileNumber())) {
            user.setMobileNumber(normalizedPhone);
            user.setMobileVerified(false);
            userRepository.save(user);
        }

        TokenService.GeneratedToken token = tokenService.createMobileVerificationToken(user, normalizedPhone);
        String verifyUrl = frontendUrl + "/verify-mobile?token=" + token.rawToken();
        boolean dispatched = smsProvider.sendVerificationLink(normalizedPhone, verifyUrl);

        Map<String, Object> result = new HashMap<>();
        result.put("smsConfigured", smsProvider.isConfigured());
        result.put("dispatched", dispatched);
        result.put("mobileVerified", Boolean.TRUE.equals(user.getMobileVerified()));
        result.put("message", dispatched
                ? "Mobile verification link sent to your phone."
                : "SMS provider is not configured. Mobile verification remains pending until an SMS provider is enabled.");
        return result;
    }

    @Transactional
    public Map<String, Object> verifyMobile(String rawToken) {
        UserToken t = tokenService.findByRawTokenAndType(rawToken, TokenType.VERIFY_MOBILE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid mobile verification token"));

        if (t.isRevoked()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mobile verification link was revoked");
        }
        if (t.isUsed()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mobile verification token already used");
        }
        if (t.isExpired()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mobile verification token expired");
        }

        AppUser user = t.getUser();
        if (t.getTargetPhone() != null && !t.getTargetPhone().isBlank()) {
            user.setMobileNumber(t.getTargetPhone());
        }
        user.setMobileVerified(true);
        userRepository.save(user);
        tokenService.markUsed(t);

        return Map.of(
                "mobileVerified", true,
                "mobileNumber", user.getMobileNumber() == null ? "" : user.getMobileNumber(),
                "message", "Mobile number verified successfully."
        );
    }

    public AuthResponse buildAuthResponse(AppUser user, String jwt, String rawRefreshToken) {
        List<String> roleNames = user.getEffectiveRoles().stream()
                .map(Enum::name)
                .toList();

        String hostStatus = hostProfileRepository.findByUser_Id(user.getId())
                .map(hp -> hp.getVerificationStatus() != null ? hp.getVerificationStatus().name() : null)
                .orElse(null);

        return AuthResponse.builder()
                .token(jwt)
                .tokenType("Bearer")
                .refreshToken(rawRefreshToken)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .emailVerified(user.isEmailVerified())
                .mobileVerified(Boolean.TRUE.equals(user.getMobileVerified()))
                .active(user.isActive())
                .suspended(user.isSuspendedFlag())
                .roles(roleNames)
                .hostApplicationStatus(hostStatus)
                .build();
    }

    private void sendVerificationEmailInternal(AppUser user) {
        TokenService.GeneratedToken verifyToken = tokenService.createEmailVerificationToken(user);
        String link = frontendUrl + "/verify?token=" + verifyToken.rawToken();

        String emailBody = "Hello " + user.getName() + ",\n\n"
                + "Thank you for registering with Car Rental!\n\n"
                + "Please click the link below to verify your email address:\n\n"
                + link + "\n\n"
                + "This link will expire in 24 hours and can only be used once.\n\n"
                + "If you did not create an account, you can safely ignore this email.\n\n"
                + "Best regards,\nCar Rental Team";

        emailService.sendEmail(
                user.getEmail(),
                "Verify Your Email - Car Rental",
                emailBody
        );
    }
}
