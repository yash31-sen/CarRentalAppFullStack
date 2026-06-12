package carRental.CarRrental.Services;

import carRental.CarRrental.Dtos.ResetPasswordRequest;
import carRental.CarRrental.Models.DriverProfile;
import carRental.CarRrental.Repositories.DriverProfileRepository;
import org.springframework.stereotype.Service;

@Service
public class DriverSelfService {

    private final DriverProfileRepository driverProfileRepository;

    public DriverSelfService(DriverProfileRepository driverProfileRepository) {
        this.driverProfileRepository = driverProfileRepository;
    }

    public DriverProfile getMyProfile(String email) {
        return driverProfileRepository.findByUser_Email(email)
                .orElseThrow(() -> new RuntimeException("Driver profile not found for: " + email));
    }

    public DriverProfile updateMyLocation(String email, String newLocation) {
        DriverProfile p = getMyProfile(email);
        p.setCurrentLocation(newLocation);
        return driverProfileRepository.save(p);
    }

    public DriverProfile updateMyStatus(String email, carRental.CarRrental.Models.DriverStatus status) {
        DriverProfile p = getMyProfile(email);
        p.setStatus(status);
        return driverProfileRepository.save(p);
    }
}
