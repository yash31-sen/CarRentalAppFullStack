package carRental.CarRrental.Services;

import carRental.CarRrental.Dtos.RegisterRequest;
import carRental.CarRrental.Models.AppUser;
import carRental.CarRrental.Models.TokenType;
import carRental.CarRrental.Models.UserRole;
import carRental.CarRrental.Models.UserToken;
import carRental.CarRrental.Repositories.AppUserRepository;
import carRental.CarRrental.Repositories.UserTokenRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import carRental.CarRrental.Dtos.AuthResponse;
import carRental.CarRrental.Dtos.LoginRequest;
import java.time.Instant;

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
                .emailVerified(false)             // not verified yet
                .build();

        AppUser savedUser = userRepository.save(user);

        UserToken verifyToken = tokenService.createEmailVerificationToken(savedUser);

        String link = "http://localhost:8080/auth/verify?token=" + verifyToken.getToken();

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

    public void verifyEmail(String token) {
        UserToken t = tokenRepository
                .findByTokenAndType(token, TokenType.VERIFY_EMAIL)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (t.isUsed()) {
            throw new RuntimeException("Token already used");
        }
        if (t.isExpired()) {
            throw new RuntimeException("Token expired");
        }

        AppUser user = t.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        t.setUsedAt(Instant.now());
        tokenRepository.save(t);
    }
}
