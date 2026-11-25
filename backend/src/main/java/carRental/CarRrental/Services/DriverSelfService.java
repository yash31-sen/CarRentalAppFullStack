package carRental.CarRrental.Services;

import carRental.CarRrental.Models.Booking;
import carRental.CarRrental.Models.BookingStatus;
import carRental.CarRrental.Models.DriverProfile;
import carRental.CarRrental.Models.DriverStatus;
import carRental.CarRrental.Repositories.BookingRepository;
import carRental.CarRrental.Repositories.DriverProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DriverSelfService {

    private final DriverProfileRepository driverProfileRepository;
    private final BookingRepository bookingRepository;

    public DriverSelfService(DriverProfileRepository driverProfileRepository,
                             BookingRepository bookingRepository) {
        this.driverProfileRepository = driverProfileRepository;
        this.bookingRepository = bookingRepository;
    }

    public DriverProfile getMyProfile(String email) {
        return driverProfileRepository.findByUser_Email(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Driver profile not found"));
    }

    public DriverProfile updateMyLocation(String email, String newLocation) {
        DriverProfile p = getMyProfile(email);
        p.setCurrentLocation(newLocation);
        return driverProfileRepository.save(p);
    }

    public DriverProfile updateMyStatus(String email, DriverStatus status) {
        DriverProfile p = getMyProfile(email);
        p.setStatus(status);
        return driverProfileRepository.save(p);
    }

    public Booking getMyActiveBooking(String email) {
        DriverProfile p = getMyProfile(email);
        return bookingRepository.findAll().stream()
                .filter(b -> b.getDriverProfile() != null
                        && b.getDriverProfile().getId().equals(p.getId())
                        && (b.getStatus() == BookingStatus.CONFIRMED
                            || b.getStatus() == BookingStatus.IN_PROGRESS))
                .findFirst()
                .orElse(null);
    }
}
