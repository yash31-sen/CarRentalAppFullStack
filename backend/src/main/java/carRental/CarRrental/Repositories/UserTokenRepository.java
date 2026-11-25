package carRental.CarRrental.Repositories;

import carRental.CarRrental.Models.AppUser;
import carRental.CarRrental.Models.TokenType;
import carRental.CarRrental.Models.UserToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserTokenRepository extends JpaRepository<UserToken, Long> {
    Optional<UserToken> findByTokenAndType(String token, TokenType type);

    List<UserToken> findAllByUserAndTypeAndUsedAtIsNullAndRevokedAtIsNull(AppUser user, TokenType type);

    List<UserToken> findAllByUserAndTypeAndUsedAtIsNull(AppUser user, TokenType type);

    long countByUserAndTypeAndCreatedAtAfter(AppUser user, TokenType type, Instant since);

    @Transactional
    void deleteByUser(AppUser user);
}
