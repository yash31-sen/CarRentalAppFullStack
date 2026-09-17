package carRental.CarRrental.Repositories;

import carRental.CarRrental.Models.AppUser;
import carRental.CarRrental.Models.Booking;
import carRental.CarRrental.Models.BookingStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUser_Id(Long userId);

    List<Booking> findByStatus(BookingStatus status);

    List<Booking> findByAssignedAdminId(Long adminId);

    List<Booking> findByPickupCityIgnoreCase(String pickupCity);

    List<Booking> findByUserOrderByCreatedAtDesc(carRental.CarRrental.Models.AppUser user);

    @Transactional
    void deleteByUser(AppUser user);

    @Query("""
    select count(b) > 0
    from Booking b
    where b.car.id = :carId
      and b.status in ('PENDING_PAYMENT','PAYMENT_SUCCESS','VERIFICATION_PENDING','CONFIRMED','IN_PROGRESS','INSPECTION_PENDING')
      and (b.startDate <= :endDate and b.endDate >= :startDate)
""")
    boolean existsOverlappingBooking(@Param("carId") Long carId,
                                     @Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate);
//
//    @Query("""
//
//SELECT COALESCE(SUM(b.totalAmount), 0)
//
//FROM Booking b
//
//""")
//    double getTotalRevenue();
}
