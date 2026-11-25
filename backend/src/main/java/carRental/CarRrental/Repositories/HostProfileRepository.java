package carRental.CarRrental.Repositories;

import carRental.CarRrental.Models.HostProfile;
import carRental.CarRrental.Models.HostVerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface HostProfileRepository extends JpaRepository<HostProfile, Long> {

    Optional<HostProfile> findByUser_Id(Long userId);

    Optional<HostProfile> findByUser_Email(String email);

    @Query("SELECT h FROM HostProfile h JOIN FETCH h.user")
    List<HostProfile> findAllWithUser();

    long countByVerificationStatus(HostVerificationStatus status);

    long countByCityIgnoreCase(String city);
}
