package carRental.CarRrental.Repositories;

import carRental.CarRrental.Models.AppUser;
import carRental.CarRrental.Models.DriverProfile;        // 👈 CORRECT import
import carRental.CarRrental.Models.DriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DriverProfileRepository
        extends JpaRepository<DriverProfile, Long> {     // 👈 Models.DriverProfile

    boolean existsByLicenseNumber(String licenseNumber);

    Optional<DriverProfile> findByUser_Email(String email);

    Optional<DriverProfile> findByUser(AppUser user);

    Optional<DriverProfile> findFirstByCurrentLocationAndStatusAndActiveTrue(
            String location,
            DriverStatus status
    );
}