package carRental.CarRrental.Repositories;

import carRental.CarRrental.Models.HostStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HostStatusHistoryRepository extends JpaRepository<HostStatusHistory, Long> {
    List<HostStatusHistory> findByHostProfile_IdOrderByCreatedAtDesc(Long hostProfileId);
}
