package carRental.CarRrental.Repositories;

import carRental.CarRrental.Models.AppUser;
import carRental.CarRrental.Models.TokenType;
import carRental.CarRrental.Models.UserToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserTokenRepository extends JpaRepository<UserToken, Long> {

    Optional<UserToken> findByTokenAndType(String token, TokenType type);
    @Transactional   //doing 2 delete operations so to make them atomic use trascational so that eother both falied or bot h succeed

    void deleteByUser(AppUser user);
}
