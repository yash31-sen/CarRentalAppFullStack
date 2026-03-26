package carRental.CarRrental.Repositories;

import carRental.CarRrental.Models.Booking;
import carRental.CarRrental.Models.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUser_Id(Long userId);

    List<Booking> findByStatus(BookingStatus status);

    List<Booking> findByAssignedAdminId(Long adminId);

    @Query("""
    select count(b) > 0
    from Booking b
    where b.car.id = :carId
      and b.status in ('PENDING_PAYMENT','PAYMENT_SUCCESS','VERIFICATION_PENDING','CONFIRMED')
      and (b.startDate <= :endDate and b.endDate >= :startDate)
""")
    boolean existsOverlappingBooking(@Param("carId") Long carId,
                                     @Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate);
}
