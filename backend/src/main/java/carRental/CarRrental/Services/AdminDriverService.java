package carRental.CarRrental.Services;

import carRental.CarRrental.Dtos.DriverCreateRequest;
import carRental.CarRrental.Dtos.DriverUpdateRequest;
import carRental.CarRrental.Models.AppUser;
import carRental.CarRrental.Models.DriverProfile;        // 👈 CORRECT
import carRental.CarRrental.Models.DriverStatus;
import carRental.CarRrental.Models.UserRole;
import carRental.CarRrental.Repositories.AppUserRepository;
import carRental.CarRrental.Repositories.DriverProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDriverService {

    private final AppUserRepository userRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public DriverProfile addDriver(DriverCreateRequest req) {

        String email = req.getEmail().trim().toLowerCase();
        String license = req.getLicenseNumber().trim().toUpperCase();

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already registered: " + email);
        }

        if (driverProfileRepository.existsByLicenseNumber(license)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Driver already exists with license: " + license);
        }

        // 1) Create AppUser for login
        AppUser driverUser = AppUser.builder()
                .name(req.getName())
                .email(email)
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role(UserRole.DRIVER)
                .emailVerified(true)
                .isActive(true)
                .build();

        AppUser savedUser = userRepository.save(driverUser);

        // 2) Create DriverProfile
        DriverProfile profile = DriverProfile.builder() // 👈 FIXED
                .user(savedUser)
                .licenseNumber(license)
                .experienceYears(req.getExperienceYears())
                .currentLocation(req.getCurrentLocation())
                .status(DriverStatus.AVAILABLE)
                .active(true)
                .build();

        return driverProfileRepository.save(profile);
    }

    public List<DriverProfile> getAllDrivers() {        // 👈 FIXED
        return driverProfileRepository.findAll();
    }

    public DriverProfile getDriverById(Long id) {      // 👈 FIXED
        return driverProfileRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Driver profile not found: " + id));
    }

    public DriverProfile updateDriver(                 // 👈 FIXED
                                                       Long id, DriverUpdateRequest req) {

        DriverProfile profile = getDriverById(id);
        AppUser user = profile.getUser();

        // update AppUser fields
        if (req.getName() != null)
            user.setName(req.getName());
        if (req.getEmail() != null)
            user.setEmail(req.getEmail().trim().toLowerCase());
        if (req.getPassword() != null)
            user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        userRepository.save(user);

        // update DriverProfile fields
        if (req.getLicenseNumber() != null)
            profile.setLicenseNumber(
                    req.getLicenseNumber().trim().toUpperCase());
        if (req.getExperienceYears() != null)
            profile.setExperienceYears(req.getExperienceYears());
        if (req.getCurrentLocation() != null)
            profile.setCurrentLocation(req.getCurrentLocation());
        if (req.getStatus() != null)
            profile.setStatus(req.getStatus());
        if (req.getActive() != null)
            profile.setActive(req.getActive());

        return driverProfileRepository.save(profile);
    }

    public void disableDriver(Long id) {
        DriverProfile profile = getDriverById(id);    // 👈 FIXED
        profile.setActive(false);
        profile.setStatus(DriverStatus.INACTIVE);
        driverProfileRepository.save(profile);

        // also disable login
        AppUser user = profile.getUser();
        user.setActive(false);
        userRepository.save(user);
    }
}