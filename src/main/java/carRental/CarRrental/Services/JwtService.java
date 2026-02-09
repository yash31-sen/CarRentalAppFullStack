package carRental.CarRrental.Services;
import carRental.CarRrental.Models.AppUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
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

        return Jwts.builder()
                .id(UUID.randomUUID().toString())        // ✅ jti, gives differnet token
                .subject(user.getEmail())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();

    }


    public String extractEmail(String token) {
        return parseToken(token).getPayload().getSubject();
    }
    public boolean isTokenValid(String token) {
        try {
            parseToken(token); // signature + expiry validate
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Jws<Claims> parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
    }
}
