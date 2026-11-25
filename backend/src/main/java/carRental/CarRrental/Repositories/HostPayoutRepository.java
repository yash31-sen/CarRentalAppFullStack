package carRental.CarRrental.Repositories;

import carRental.CarRrental.Models.HostPayout;
import carRental.CarRrental.Models.PayoutStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface HostPayoutRepository extends JpaRepository<HostPayout, Long> {

    List<HostPayout> findByHostProfile_IdOrderByCreatedAtDesc(Long hostProfileId);

    long countByStatus(PayoutStatus status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM HostPayout p WHERE p.status = 'PENDING'")
    BigDecimal sumPendingPayoutAmount();
}
