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

    @Query("""
        SELECT COALESCE(SUM(COALESCE(b.grossAmount, CAST(b.finalPrice AS BigDecimal))), 0)
        FROM Booking b
        WHERE b.status IN ('PAYMENT_SUCCESS','VERIFICATION_PENDING','DRIVER_ASSIGNMENT_PENDING','CONFIRMED','IN_PROGRESS','INSPECTION_PENDING','COMPLETED')
    """)
    java.math.BigDecimal getTotalRevenue();

    @Query("""
        SELECT COALESCE(SUM(COALESCE(b.platformCommissionAmount, 0) + COALESCE(b.platformFeeAmount, 0)), 0)
        FROM Booking b
        WHERE b.status IN ('PAYMENT_SUCCESS','VERIFICATION_PENDING','DRIVER_ASSIGNMENT_PENDING','CONFIRMED','IN_PROGRESS','INSPECTION_PENDING','COMPLETED')
    """)
    java.math.BigDecimal getTotalPlatformEarnings();

    @Query("""
        SELECT COUNT(b)
        FROM Booking b
        WHERE b.status IN ('PAYMENT_SUCCESS','VERIFICATION_PENDING','DRIVER_ASSIGNMENT_PENDING','CONFIRMED','IN_PROGRESS','INSPECTION_PENDING')
    """)
    long countActiveBookings();

    @Query("""
        SELECT COUNT(b)
        FROM Booking b
        WHERE UPPER(b.pickupCity) = UPPER(:city)
          AND b.status IN ('PAYMENT_SUCCESS','VERIFICATION_PENDING','DRIVER_ASSIGNMENT_PENDING','CONFIRMED','IN_PROGRESS','INSPECTION_PENDING')
    """)
    long countActiveBookingsByCity(@Param("city") String city);

    long countByPickupCityIgnoreCaseOrDropCityIgnoreCase(String pickupCity, String dropCity);

    List<Booking> findByCar_Host_IdOrderByCreatedAtDesc(Long hostId);

    @Query("""
        SELECT COALESCE(SUM(COALESCE(b.hostEarningsAmount, 0)), 0)
        FROM Booking b
        WHERE b.car.host.id = :hostId
          AND b.status IN ('PAYMENT_SUCCESS','VERIFICATION_PENDING','DRIVER_ASSIGNMENT_PENDING','CONFIRMED','IN_PROGRESS','INSPECTION_PENDING','COMPLETED')
    """)
    java.math.BigDecimal getTotalHostEarnings(@Param("hostId") Long hostId);

    @Query("SELECT b FROM Booking b JOIN FETCH b.user JOIN FETCH b.car c LEFT JOIN FETCH c.host h LEFT JOIN FETCH h.user WHERE b.bookingType = 'SELF_DRIVE' AND UPPER(b.pickupCity) IN :cities ORDER BY b.createdAt DESC")
    List<Booking> findSelfDriveBookingsByCities(@Param("cities") List<String> upperCities);

    @Query("SELECT b FROM Booking b JOIN FETCH b.user JOIN FETCH b.car c LEFT JOIN FETCH c.host h LEFT JOIN FETCH h.user WHERE b.bookingType = 'SELF_DRIVE' ORDER BY b.createdAt DESC")
    List<Booking> findAllSelfDriveBookings();

    List<Booking> findByCar_IdOrderByCreatedAtDesc(Long carId);
}
