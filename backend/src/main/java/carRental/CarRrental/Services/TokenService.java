package carRental.CarRrental.Services;

import carRental.CarRrental.Models.AppUser;
import carRental.CarRrental.Models.TokenType;
import carRental.CarRrental.Models.UserToken;
import carRental.CarRrental.Repositories.UserTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

@Service
public class TokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int TOKEN_BYTE_LENGTH = 32; // 256-bit cryptographically secure random token
    private static final int MAX_VERIFICATION_TOKENS_PER_HOUR = 5;

    public record GeneratedToken(String rawToken, UserToken entity) {}

    private final UserTokenRepository tokenRepository;

    public TokenService(UserTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Transactional
    public GeneratedToken createEmailVerificationToken(AppUser user) {
        enforceHourlyRateLimit(user, TokenType.VERIFY_EMAIL, MAX_VERIFICATION_TOKENS_PER_HOUR);
        revokeActiveTokens(user, TokenType.VERIFY_EMAIL);
        return createToken(user, TokenType.VERIFY_EMAIL, Duration.ofHours(24), null);
    }

    @Transactional
    public GeneratedToken createMobileVerificationToken(AppUser user, String targetPhone) {
        enforceHourlyRateLimit(user, TokenType.VERIFY_MOBILE, MAX_VERIFICATION_TOKENS_PER_HOUR);
        revokeActiveTokens(user, TokenType.VERIFY_MOBILE);
        return createToken(user, TokenType.VERIFY_MOBILE, Duration.ofMinutes(15), targetPhone);
    }

    @Transactional
    public GeneratedToken createPasswordResetToken(AppUser user) {
        enforceHourlyRateLimit(user, TokenType.RESET_PASSWORD, MAX_VERIFICATION_TOKENS_PER_HOUR);
        revokeActiveTokens(user, TokenType.RESET_PASSWORD);
        return createToken(user, TokenType.RESET_PASSWORD, Duration.ofMinutes(15), null);
    }

    @Transactional
    public GeneratedToken createAccountSetupToken(AppUser user) {
        revokeActiveTokens(user, TokenType.ACCOUNT_SETUP);
        return createToken(user, TokenType.ACCOUNT_SETUP, Duration.ofHours(24), null);
    }

    @Transactional
    public GeneratedToken createRefreshToken(AppUser user) {
        return createToken(user, TokenType.REFRESH_TOKEN, Duration.ofDays(7), null);
    }

    public Optional<UserToken> findByRawTokenAndType(String rawToken, TokenType type) {
        if (rawToken == null || rawToken.isBlank()) {
            return Optional.empty();
        }
        String hashed = hashToken(rawToken.trim());
        return tokenRepository.findByTokenAndType(hashed, type);
    }

    @Transactional
    public void revokeActiveTokens(AppUser user, TokenType type) {
        List<UserToken> activeTokens = tokenRepository
                .findAllByUserAndTypeAndUsedAtIsNullAndRevokedAtIsNull(user, type);
        if (activeTokens.isEmpty()) {
            return;
        }
        Instant now = Instant.now();
        for (UserToken token : activeTokens) {
            token.setRevokedAt(now);
        }
        tokenRepository.saveAll(activeTokens);
    }

    @Transactional
    public void markUsed(UserToken token) {
        token.setUsedAt(Instant.now());
        tokenRepository.save(token);
    }

    public String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", ex);
        }
    }

    private void enforceHourlyRateLimit(AppUser user, TokenType type, int maxAllowed) {
        Instant oneHourAgo = Instant.now().minus(Duration.ofHours(1));
        long recentCount = tokenRepository.countByUserAndTypeAndCreatedAtAfter(user, type, oneHourAgo);
        if (recentCount >= maxAllowed) {
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Too many verification requests. Please try again later."
            );
        }
    }

    private GeneratedToken createToken(AppUser user, TokenType type, Duration ttl, String targetPhone) {
        byte[] bytes = new byte[TOKEN_BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String hashedToken = hashToken(rawToken);

        UserToken token = UserToken.builder()
                .user(user)
                .token(hashedToken)
                .type(type)
                .targetPhone(targetPhone)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(ttl))
                .usedAt(null)
                .revokedAt(null)
                .build();

        UserToken saved = tokenRepository.save(token);
        return new GeneratedToken(rawToken, saved);
    }
}
