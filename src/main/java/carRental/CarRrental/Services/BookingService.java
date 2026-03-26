package carRental.CarRrental.Services;

import carRental.CarRrental.Dtos.BookingCreateRequest;
import carRental.CarRrental.Models.*;
import carRental.CarRrental.Repositories.BookingRepository;
import carRental.CarRrental.Repositories.CarRepository;
import carRental.CarRrental.Repositories.AppUserRepository;
import carRental.CarRrental.Repositories.DriverProfileRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final CarRepository carRepository;
    private final AppUserRepository userRepository;
    private final DriverProfileRepository driverProfileRepository;

    public BookingService(BookingRepository bookingRepository,
                          CarRepository carRepository,
                          AppUserRepository userRepository,
                          DriverProfileRepository driverProfileRepository) {
        this.bookingRepository = bookingRepository;
        this.carRepository = carRepository;
        this.userRepository = userRepository;
        this.driverProfileRepository = driverProfileRepository;
    }

    public Booking createBooking(String userEmail, BookingCreateRequest req) {

        AppUser user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Car car = carRepository.findById(req.getCarId())
                .orElseThrow(() -> new RuntimeException("Car not found"));

        if (!car.isActive()) {
            throw new RuntimeException("Car is not active");
        }

        if (req.getStartDate().isAfter(req.getEndDate())) {
            throw new RuntimeException("Invalid date range");
        }

        if (req.getBookingType() == BookingType.SELF_DRIVE &&
                (req.getDrivingLicenseNumber() == null || req.getDrivingLicenseNumber().isBlank())) {
            throw new RuntimeException("Driving license required for self-drive booking");
        }

        boolean conflict = bookingRepository.existsOverlappingBooking(
                car.getId(),
                req.getStartDate(),
                req.getEndDate()
        );

        if (conflict) {
            throw new RuntimeException("Car is not available for selected dates");
        }

        long days = ChronoUnit.DAYS.between(req.getStartDate(), req.getEndDate()) + 1;
        double totalPrice = days * car.getPricePerDay();

        Booking booking = Booking.builder()
                .user(user)
                .car(car)
                .bookingType(req.getBookingType())
                .status(BookingStatus.PENDING_PAYMENT)
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .pickupLocation(req.getPickupLocation())
                .dropLocation(req.getDropLocation())
                .totalPrice(totalPrice)
                .drivingLicenseNumber(req.getDrivingLicenseNumber())
                .build();

        return bookingRepository.save(booking);
    }


    public Booking markPaymentSuccess(Long bookingId, String userEmail, String paymentRef) {

        Booking b = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!b.getUser().getEmail().equalsIgnoreCase(userEmail)) {
            throw new RuntimeException("Not allowed");
        }

        if (b.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new RuntimeException("Payment already processed or invalid state");
        }

        b.setPaymentRef(paymentRef);
        b.setPaymentAt(Instant.now());

        // ============================
        // SELF DRIVE FLOW
        // ============================
        if (b.getBookingType() == BookingType.SELF_DRIVE) {
            b.setStatus(BookingStatus.VERIFICATION_PENDING);
            return bookingRepository.save(b);
        }

        // ============================
        // WITH DRIVER FLOW
        // ============================

        DriverProfile driver = driverProfileRepository
                .findFirstByCurrentLocationAndStatusAndActiveTrue(
                        b.getPickupLocation(),
                        DriverStatus.AVAILABLE
                )
                .orElse(null);

        if (driver != null) {
            // Assign driver
            b.setDriverProfile(driver);
            b.setStatus(BookingStatus.CONFIRMED);

            // Mark driver busy
            driver.setStatus(DriverStatus.ON_TRIP);
            driverProfileRepository.save(driver);

        } else {
            // No driver available
            b.setStatus(BookingStatus.DRIVER_ASSIGNMENT_PENDING);
        }

        return bookingRepository.save(b);
    }
}
