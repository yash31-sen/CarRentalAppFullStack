package carRental.CarRrental.Services;

import carRental.CarRrental.Models.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expiryMinutes;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiryMinutes}") long expiryMinutes
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiryMinutes = expiryMinutes;
    }

    public String generateToken(AppUser user) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expiryMinutes * 60);
        List<String> roleNames = user.getEffectiveRoles().stream()
                .map(Enum::name)
                .toList();
        int tokenVersion = user.getTokenVersion() == null ? 0 : user.getTokenVersion();

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getEmail())
                .claim("role", user.getRole().name())
                .claim("roles", roleNames)
                .claim("tokenVersion", tokenVersion)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public String extractEmail(String token) {
        return parse(token).getPayload().getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            parse(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String extractRole(String token) {
        return parse(token).getPayload().get("role", String.class);
    }

    public Integer extractTokenVersion(String token) {
        Object val = parse(token).getPayload().get("tokenVersion");
        if (val instanceof Number number) {
            return number.intValue();
        }
        return null;
    }

    private Jws<Claims> parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
    }
}
