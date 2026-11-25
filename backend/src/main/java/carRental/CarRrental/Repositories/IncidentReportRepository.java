package carRental.CarRrental.Repositories;

import carRental.CarRrental.Models.IncidentReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IncidentReportRepository extends JpaRepository<IncidentReport, Long> {

    @Query("SELECT i FROM IncidentReport i JOIN FETCH i.reportedBy WHERE i.reportedUser.id = :userId ORDER BY i.createdAt DESC")
    List<IncidentReport> findByReportedUserIdWithReporter(@Param("userId") Long userId);

    @Query("SELECT i FROM IncidentReport i JOIN FETCH i.reportedBy JOIN FETCH i.reportedUser LEFT JOIN FETCH i.booking LEFT JOIN FETCH i.assignedSupportAgent ORDER BY i.createdAt DESC")
    List<IncidentReport> findAllWithDetails();

    List<IncidentReport> findByBooking_IdOrderByCreatedAtDesc(Long bookingId);
}
