package carRental.CarRrental.Services;

import carRental.CarRrental.Dtos.DriverCreateRequest;
import carRental.CarRrental.Dtos.DriverUpdateRequest;
import carRental.CarRrental.Models.*;
import carRental.CarRrental.Repositories.AppUserRepository;
import carRental.CarRrental.Repositories.DriverProfileRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminDriverService {

    private final AppUserRepository userRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminDriverService(AppUserRepository userRepository,
                              DriverProfileRepository driverProfileRepository,
                              PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.driverProfileRepository = driverProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public DriverProfile addDriver(DriverCreateRequest req) {
        String email = req.getEmail().trim().toLowerCase();
        String license = req.getLicenseNumber().trim().toUpperCase();

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered: " + email);
        }
        if (driverProfileRepository.existsByLicenseNumber(license)) {
            throw new RuntimeException("Driver already exists with license: " + license);
        }

        // 1) Create AppUser for login
        AppUser driverUser = AppUser.builder()
                .name(req.getName())
                .email(email)
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role(UserRole.DRIVER)
                .emailVerified(true) // admin created
                .build();

        AppUser savedUser = userRepository.save(driverUser);

        // 2) Create DriverProfile
        DriverProfile profile = DriverProfile.builder()
                .user(savedUser)
                .licenseNumber(license)
                .experienceYears(req.getExperienceYears())
                .currentLocation(req.getCurrentLocation())
                .status(DriverStatus.AVAILABLE)
                .active(true)
                .build();

        return driverProfileRepository.save(profile);
    }

    public List<DriverProfile> getAllDrivers() {
        return driverProfileRepository.findAll();
    }

    public DriverProfile getDriverById(Long id) {
        return driverProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Driver profile not found: " + id));
    }

    public DriverProfile updateDriver(Long id, DriverUpdateRequest req) {
        DriverProfile profile = getDriverById(id);
        AppUser user = profile.getUser();

        // update AppUser
        if (req.getName() != null) user.setName(req.getName());
        if (req.getEmail() != null) user.setEmail(req.getEmail().trim().toLowerCase());
        if (req.getPassword() != null) user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        userRepository.save(user);

        // update DriverProfile
        if (req.getLicenseNumber() != null) profile.setLicenseNumber(req.getLicenseNumber().trim().toUpperCase());
        if (req.getExperienceYears() != null) profile.setExperienceYears(req.getExperienceYears());
        if (req.getCurrentLocation() != null) profile.setCurrentLocation(req.getCurrentLocation());
        if (req.getStatus() != null) profile.setStatus(req.getStatus());
        if (req.getActive() != null) profile.setActive(req.getActive());

        return driverProfileRepository.save(profile);
    }

    public void disableDriver(Long id) {
        DriverProfile profile = getDriverById(id);
        profile.setActive(false);
        driverProfileRepository.save(profile);

        // optional: also disable login
        // profile.getUser().setEmailVerified(false); or add a separate "enabled" field in AppUser
    }
}
