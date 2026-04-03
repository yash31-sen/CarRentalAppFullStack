package carRental.CarRrental.Repositories;

import carRental.CarRrental.Models.DriverProfile;
import carRental.CarRrental.Models.DriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DriverProfileRepository extends JpaRepository<DriverProfile, Long> {
    boolean existsByLicenseNumber(String licenseNumber);
    Optional<DriverProfile> findByUser_Email(String email); // driver self endpoints के लिए useful
    Optional<DriverProfile> findFirstByCurrentLocationAndStatusAndActiveTrue(
            String location,
            DriverStatus status
    );
}
