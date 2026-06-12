package carRental.CarRrental.Services;

import carRental.CarRrental.Dtos.*;
import carRental.CarRrental.Models.AppUser;
import carRental.CarRrental.Models.TokenType;
import carRental.CarRrental.Models.UserRole;
import carRental.CarRrental.Models.UserToken;
import carRental.CarRrental.Repositories.AppUserRepository;
import carRental.CarRrental.Repositories.UserTokenRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

import carRental.CarRrental.Models.TokenType;
import carRental.CarRrental.Models.UserRole;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final JwtService jwtService;

    private final AppUserRepository userRepository;
    private final UserTokenRepository tokenRepository;
    private final TokenService tokenService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            AppUserRepository userRepository,
            UserTokenRepository tokenRepository,
            TokenService tokenService,
            EmailService emailService,
            PasswordEncoder passwordEncoder,
            JwtService jwtService

            ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.tokenService = tokenService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService=jwtService;

    }

    public void register(RegisterRequest req) {
        String email = req.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }

        AppUser user = AppUser.builder()
                .name(req.getName())
                .email(email)
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role(UserRole.USER)              // force USER
                .emailVerified(false)   // not verified yet
                .isActive(true)
                .build();

        AppUser savedUser = userRepository.save(user);

        UserToken verifyToken = tokenService.createEmailVerificationToken(savedUser);

        String link = "http://localhost:4200/verify?token=" + verifyToken.getToken();

        emailService.sendEmail(
                savedUser.getEmail(),
                "Verify your email",
                "Click to verify your account:\n" + link
        );
    }

    public AuthResponse login(LoginRequest req) {

        String email = req.getEmail().trim().toLowerCase();

        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        // block login if email not verified
        if (!user.isEmailVerified()) {
            throw new RuntimeException("Please verify your email first");
        }

        // password check (raw vs hashed)
        boolean ok = passwordEncoder.matches(req.getPassword(), user.getPasswordHash());
        if (!ok) {
            throw new RuntimeException("Invalid credentials");
        }

        String jwt = jwtService.generateToken(user);

        return new AuthResponse(jwt, "Bearer");
    }

    public AuthResponse verifyEmail(String token) {

        UserToken t = tokenRepository
                .findByTokenAndType(token, TokenType.VERIFY_EMAIL)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid token"));

        if (t.isUsed()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Token already used");
        }

        if (t.isExpired()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Token expired");
        }

        // verify email
        AppUser user = t.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        // mark token used
        t.setUsedAt(Instant.now());
        tokenRepository.save(t);

        // 👇 NEW - generate JWT and return
        // user is now verified and logged in!
        String jwt = jwtService.generateToken(user);
        return new AuthResponse(jwt, "Bearer");  // ← return token!
    }
    public void forgotPassword(ForgotPasswordRequest req) {
        String email = req.getEmail().trim().toLowerCase();
        // IMPORTANT: response generic to prevent user enumeration
        userRepository.findByEmail(email).ifPresent(user -> {
            var resetToken = tokenService.createPasswordResetToken(user);
            String link = "http://localhost:8080/auth/reset-password?token=" + resetToken.getToken();
            emailService.sendEmail(
                    user.getEmail(),
                    "Reset your password",
                    "Click to reset password (valid 15 minutes):\n" + link
            );
        });
    }
// ...
    public void resetPassword(ResetPasswordRequest req) {
        UserToken t = tokenRepository
                .findByTokenAndType(req.getToken(), TokenType.RESET_PASSWORD)
                .orElseThrow(() -> new RuntimeException("Invalid token"));
        if (t.isUsed()) throw new RuntimeException("Token already used");
        if (t.isExpired()) throw new RuntimeException("Token expired");
        AppUser user = t.getUser();
        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
        t.setUsedAt(Instant.now());
        tokenRepository.save(t);
    }

}
