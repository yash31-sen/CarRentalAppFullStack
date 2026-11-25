package carRental.CarRrental.Repositories;

import carRental.CarRrental.Models.ListingHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ListingHistoryRepository extends JpaRepository<ListingHistory, Long> {
    List<ListingHistory> findByCar_IdOrderByCreatedAtDesc(Long carId);
}
