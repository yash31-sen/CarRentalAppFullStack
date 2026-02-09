package carRental.CarRrental.Repositories;

import carRental.CarRrental.Models.TokenType;
import carRental.CarRrental.Models.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserTokenRepository extends JpaRepository<UserToken, Long> {

    Optional<UserToken> findByTokenAndType(String token, TokenType type);
}
