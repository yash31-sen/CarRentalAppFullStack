package carRental.CarRrental.Services;

import carRental.CarRrental.Dtos.BookingCreateRequest;
import carRental.CarRrental.Dtos.RegisterRequest;
import carRental.CarRrental.Dtos.ResetPasswordRequest;
import carRental.CarRrental.Models.*;
import carRental.CarRrental.Repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final CarRepository carRepository;
    private final AppUserRepository userRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final ServiceCityRepository serviceCityRepository;
    private final CarPricingRepository carPricingRepository;  // 👈 ADDED

    // =============================================
    // 1. CREATE BOOKING
    // =============================================
    public Booking createBooking(String userEmail,
                                 BookingCreateRequest req) {

        // Step 1 - find user
        AppUser user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        // Step 2 - find car
        Car car = carRepository.findById(req.getCarId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Car not found"));

        // Step 3 - check car is active
        if (!car.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Car is not active");
        }

        // Step 4 - check car is available
        if (car.getStatus() != CarStatus.AVAILABLE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Car is not available. Current status: "
                            + car.getStatus());
        }

        // Step 5 - validate dates
        if (req.getStartDate().isAfter(req.getEndDate())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid date range");
        }

        // Step 6 - validate driving license for self drive
        if (req.getBookingType() == RegisterRequest.BookingType.SELF_DRIVE &&
                (req.getDrivingLicenseNumber() == null ||
                        req.getDrivingLicenseNumber().isBlank())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Driving license required for self-drive");
        }

        // Step 7 - validate trip type
        if (req.getTripType() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Trip type is required");
        }

        // Step 8 - validate cities
        validateCities(req);

        // Step 9 - check overlapping bookings
        boolean conflict = bookingRepository.existsOverlappingBooking(
                car.getId(),
                req.getStartDate(),
                req.getEndDate());

        if (conflict) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Car is not available for selected dates");
        }

        // Step 10 - get pricing rule for this car class
        CarPricing pricing = carPricingRepository
                .findByCarClass(car.getCarClass())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "No pricing rule found for "
                                + car.getCarClass()
                                + ". Contact Super Admin."));

        // Step 11 - calculate price
        long days = ChronoUnit.DAYS.between(
                req.getStartDate(),
                req.getEndDate()) + 1;

        double baseFare    = pricing.getBaseFare();
        double timeCharge  = days * car.getPricePerDay();
        double oneWayFee   = 0.0;

        if (req.getTripType() == TripType.ONE_WAY) {
            ServiceCity pickupCity = serviceCityRepository
                    .findByCityName(req.getPickupCity())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Pickup city not found"));
            oneWayFee = pickupCity.getOneWayFee();
        }

        double estimatedPrice = baseFare + timeCharge + oneWayFee;

        // Step 12 - build and save booking
        Booking booking = Booking.builder()
                .user(user)
                .car(car)
                .bookingType(req.getBookingType())
                .tripType(req.getTripType())
                .status(BookingStatus.PENDING_PAYMENT)
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .pickupCity(req.getPickupCity())
                .dropCity(req.getDropCity())
                .pickupLocation(req.getPickupLocation())
                .dropLocation(req.getDropLocation())
                .baseFare(baseFare)
                .basePrice(timeCharge)
                .oneWayFee(oneWayFee)
                .estimatedPrice(estimatedPrice)
                .extraKmCharge(0.0)
                .finalPrice(estimatedPrice)
                .totalPrice(estimatedPrice)
                .drivingLicenseNumber(req.getDrivingLicenseNumber())
                .build();

        return bookingRepository.save(booking); // 👈 FIXED
    }

    // =============================================
    // 2. MARK PAYMENT SUCCESS
    // =============================================
    public Booking markPaymentSuccess(Long bookingId,
                                      String userEmail,
                                      String paymentRef) {

        Booking b = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Booking not found"));

        if (!b.getUser().getEmail().equalsIgnoreCase(userEmail)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Not allowed");
        }

        if (b.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Payment already processed or invalid state");
        }

        b.setPaymentRef(paymentRef);
        b.setPaymentAt(Instant.now());
        b.setStatus(BookingStatus.PAYMENT_SUCCESS);

        // ============================
        // SELF DRIVE FLOW
        // ============================
        if (b.getBookingType() == RegisterRequest.BookingType.SELF_DRIVE) {
            b.setStatus(BookingStatus.VERIFICATION_PENDING);
            return bookingRepository.save(b);
        }

        // ============================
        // WITH DRIVER FLOW
        // ============================
        DriverProfile driver = driverProfileRepository
                .findFirstByCurrentLocationAndStatusAndActiveTrue(
                        b.getPickupCity(),
                        DriverStatus.AVAILABLE)
                .orElse(null);

        if (driver != null) {
            b.setDriverProfile(driver);
            b.setStatus(BookingStatus.CONFIRMED);
            driver.setStatus(DriverStatus.ON_TRIP);
            driverProfileRepository.save(driver);
        } else {
            b.setStatus(BookingStatus.DRIVER_ASSIGNMENT_PENDING);
        }

        return bookingRepository.save(b);
    }

    // =============================================
    // 3. START TRIP
    // =============================================
    public Booking startTrip(Long bookingId,
                             Double startOdometer,
                             String startOdometerPhoto) {

        Booking b = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Booking not found"));

        if (b.getStatus() != BookingStatus.CONFIRMED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Booking is not confirmed yet");
        }

        // set start odometer
        b.setStartOdometer(startOdometer);           // 👈 NEW
        b.setStartOdometerPhoto(startOdometerPhoto); // 👈 NEW
        b.setStatus(BookingStatus.IN_PROGRESS);
        b.setTripStartedAt(Instant.now());

        // update car status
        Car car = b.getCar();
        car.setStatus(CarStatus.BOOKED);
        carRepository.save(car);

        return bookingRepository.save(b);
    }

    // =============================================
    // 4. END TRIP
    // =============================================
    public Booking endTrip(Long bookingId,
                           Double endOdometer,
                           String endOdometerPhoto) {

        Booking b = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Booking not found"));

        if (b.getStatus() != BookingStatus.IN_PROGRESS) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Trip is not in progress");
        }

        // Step 1 - set end odometer
        b.setEndOdometer(endOdometer);
        b.setEndOdometerPhoto(endOdometerPhoto);

        // Step 2 - calculate km and extra charges
        if (b.getStartOdometer() != null) {

            double totalKm = endOdometer - b.getStartOdometer();
            b.setTotalKmDriven(totalKm);

            CarPricing pricing = carPricingRepository
                    .findByCarClass(b.getCar().getCarClass())
                    .orElse(null);

            if (pricing != null) {
                long days = ChronoUnit.DAYS.between(
                        b.getStartDate(),
                        b.getEndDate()) + 1;

                double freeKm      = days * pricing.getFreeKmPerDay();
                double extraKm     = Math.max(0, totalKm - freeKm);
                double extraCharge = extraKm * pricing.getExtraKmRate();

                b.setExtraKmCharge(extraCharge);
                b.setFinalPrice(b.getEstimatedPrice() + extraCharge);
            }
        }

        // Step 3 - update booking status
        b.setStatus(BookingStatus.INSPECTION_PENDING);
        b.setTripEndedAt(Instant.now());

        // Step 4 - free up driver
        if (b.getDriverProfile() != null) {
            DriverProfile driver = b.getDriverProfile();
            driver.setStatus(DriverStatus.AVAILABLE);
            driverProfileRepository.save(driver);
        }

        // Step 5 - update car
        Car car = b.getCar();
        car.setStatus(CarStatus.PENDING_INSPECTION);

        if (b.getTripType() == TripType.ONE_WAY) {
            car.setCurrentCity(b.getDropCity());
            ServiceCity dropCity = serviceCityRepository
                    .findByCityName(b.getDropCity())
                    .orElse(null);
            if (dropCity != null) {
                car.setCurrentAdmin(dropCity.getAdmin());
                car.setCurrentLocation(
                        dropCity.getParkingAddress());
            }
        }

        carRepository.save(car);
        return bookingRepository.save(b);
    }

    // =============================================
    // 5. COMPLETE BOOKING (after inspection)
    // =============================================
    public Booking completeBooking(Long bookingId) {

        Booking b = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Booking not found"));

        if (b.getStatus() != BookingStatus.INSPECTION_PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Booking is not in inspection state");
        }

        b.setStatus(BookingStatus.COMPLETED);

        Car car = b.getCar();
        car.setStatus(CarStatus.AVAILABLE);
        carRepository.save(car);

        return bookingRepository.save(b);
    }

    // =============================================
    // 6. CANCEL BOOKING
    // =============================================
    public Booking cancelBooking(Long bookingId,
                                 String userEmail) {

        Booking b = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Booking not found"));

        if (!b.getUser().getEmail().equalsIgnoreCase(userEmail)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Not allowed");
        }

        if (b.getStatus() == BookingStatus.IN_PROGRESS ||
                b.getStatus() == BookingStatus.COMPLETED ||
                b.getStatus() == BookingStatus.INSPECTION_PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot cancel booking at this stage");
        }

        double refundAmount = calculateRefund(b);

        if (b.getDriverProfile() != null) {
            DriverProfile driver = b.getDriverProfile();
            driver.setStatus(DriverStatus.AVAILABLE);
            driverProfileRepository.save(driver);
        }

        Car car = b.getCar();
        car.setStatus(CarStatus.AVAILABLE);
        carRepository.save(car);

        b.setStatus(BookingStatus.CANCELLED);
        return bookingRepository.save(b);
    }

    // =============================================
    // PRIVATE HELPER METHODS
    // =============================================
    private void validateCities(BookingCreateRequest req) {

        boolean pickupExists = serviceCityRepository
                .existsByCityNameAndActiveTrue(req.getPickupCity());

        if (!pickupExists) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Service not available in "
                            + req.getPickupCity());
        }

        if (req.getTripType() == TripType.ONE_WAY) {

            if (req.getPickupCity().equalsIgnoreCase(
                    req.getDropCity())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Pickup and drop city must be " +
                                "different for one way trip");
            }

            boolean dropExists = serviceCityRepository
                    .existsByCityNameAndActiveTrue(req.getDropCity());

            if (!dropExists) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "One way trips to "
                                + req.getDropCity()
                                + " are not available yet");
            }
        }

        if (req.getTripType() == TripType.ROUND_TRIP) {

            if (!req.getPickupCity().equalsIgnoreCase(
                    req.getDropCity())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Pickup and drop city must be " +
                                "same for round trip");
            }
        }
    }

    private double calculateRefund(Booking b) {

        if (b.getStatus() == BookingStatus.PENDING_PAYMENT ||
                b.getStatus() == BookingStatus.PAYMENT_SUCCESS ||
                b.getStatus() == BookingStatus.VERIFICATION_PENDING ||
                b.getStatus() == BookingStatus.DRIVER_ASSIGNMENT_PENDING) {
            return b.getTotalPrice();
        }

        if (b.getStatus() == BookingStatus.CONFIRMED) {

            long hoursUntilTrip = ChronoUnit.HOURS.between(
                    Instant.now(),
                    b.getStartDate()
                            .atStartOfDay()
                            .toInstant(java.time.ZoneOffset.UTC));

            if (hoursUntilTrip >= 48) return b.getTotalPrice();
            else if (hoursUntilTrip >= 24) return b.getTotalPrice() * 0.5;
            else return 0.0;
        }

        return 0.0;
    }
}