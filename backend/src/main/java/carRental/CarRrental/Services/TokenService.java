package carRental.CarRrental.Services;

import carRental.CarRrental.Models.AppUser;
import carRental.CarRrental.Models.TokenType;
import carRental.CarRrental.Models.UserToken;
import carRental.CarRrental.Repositories.UserTokenRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class TokenService {

    private final UserTokenRepository tokenRepository;

    public TokenService(UserTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    // Email verification token (longer expiry)
    public UserToken createEmailVerificationToken(AppUser user) {
        return createToken(user, TokenType.VERIFY_EMAIL, Duration.ofHours(24));
    }

    // Password reset token (short expiry)
    public UserToken createPasswordResetToken(AppUser user) {
        return createToken(user, TokenType.RESET_PASSWORD, Duration.ofMinutes(15));
    }

    // Refresh token (long expiry)
    public UserToken createRefreshToken(AppUser user) {
        return createToken(user, TokenType.REFRESH_TOKEN, Duration.ofDays(7));
    }

    private UserToken createToken(AppUser user, TokenType type, Duration ttl) {
        String rawToken = UUID.randomUUID().toString(); // simple + unique

        UserToken token = UserToken.builder()
                .user(user)
                .token(rawToken)
                .type(type)
                .expiresAt(Instant.now().plus(ttl))
                .usedAt(null)
                .build();

        return tokenRepository.save(token);
    }
}
