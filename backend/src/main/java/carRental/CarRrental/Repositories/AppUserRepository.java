package carRental.CarRrental.Repositories;

import carRental.CarRrental.Models.AppUser;
import carRental.CarRrental.Models.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    long countByRole(UserRole role);
    Optional<AppUser> findByEmail(String email);

    boolean existsByEmail(String email);

    List<AppUser> findByRole(UserRole role);

    List<AppUser> findByRoleAndIsActive(UserRole role, boolean isActive);

    // 👇 NEW - find admin by city
    Optional<AppUser> findByRoleAndCity(UserRole role, String city);
}