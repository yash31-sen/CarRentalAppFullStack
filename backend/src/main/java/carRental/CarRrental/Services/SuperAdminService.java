package carRental.CarRrental.Services;

import carRental.CarRrental.Dtos.AdminSummaryDto;
import carRental.CarRrental.Dtos.CarPricingRequest;
import carRental.CarRrental.Dtos.CreateAdminRequest;
import carRental.CarRrental.Dtos.DashboardStatsResponse;
import carRental.CarRrental.Dtos.ServiceCityRequest;
import carRental.CarRrental.Models.*;
import carRental.CarRrental.Repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SuperAdminService {
    @Value("${app.frontend.url}")
    private String frontendUrl;

    private final CarPricingRepository carPricingRepository;
    private final ServiceCityRepository serviceCityRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserTokenRepository userTokenRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final HostProfileRepository hostProfileRepository;
    private final HostPayoutRepository hostPayoutRepository;
    private final CarRepository carRepository;
    private final BookingRepository bookingRepository;
    private final TokenService tokenService;
    private final EmailService emailService;
    private final PricingValidationService pricingValidationService;
    private final AuditLogService auditLogService;

    // ✅ 1. Create Admin
    @Transactional
    public boolean createAdmin(CreateAdminRequest req) {

        String email = req.getEmail().trim().toLowerCase();

        if (appUserRepository.existsByEmail(email)) {
            return false;
        }

        if (req.getCity() == null || req.getCity().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "City is required for admin"
            );
        }

        String rawPassword = (req.getPassword() != null && !req.getPassword().isBlank())
                ? req.getPassword()
                : java.util.UUID.randomUUID().toString();

        AppUser admin = AppUser.builder()
                .name(req.getName())
                .email(email)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .role(UserRole.ADMIN)
                .emailVerified(false) // Will be set to true when they set up password
                .isActive(true)
                .city(req.getCity().trim())
                .build();

        AppUser savedAdmin = appUserRepository.save(admin);

        // Create setup token and send email invite
        TokenService.GeneratedToken setupToken = tokenService.createAccountSetupToken(savedAdmin);
        String link = frontendUrl + "/setup-account?token=" + setupToken.rawToken();

        emailService.sendEmail(
                savedAdmin.getEmail(),
                "Set Up Your Admin Account",
                "Hello " + savedAdmin.getName() + ",\n\n" +
                        "You have been added as an Admin in the Car Rental system.\n" +
                        "Please click the link below to set up your password and activate your account:\n" +
                        link + "\n\n" +
                        "This link will expire in 24 hours.\n\n" +
                        "Best regards,\nCar Rental Team"
        );

        auditLogService.log("CREATE_ADMIN", "AppUser", String.valueOf(savedAdmin.getId()),
                "Created admin " + savedAdmin.getEmail() + " for city " + savedAdmin.getCity());

        return true;
    }

    // ✅ 2. Get All Admins (DTO with assignedCities & registration date)
    public List<AdminSummaryDto> getAllAdminSummaries() {
        return appUserRepository.findByRole(UserRole.ADMIN).stream()
                .map(admin -> {
                    List<String> assigned = serviceCityRepository.findByAdmin_Id(admin.getId())
                            .map(c -> List.of(c.getCityName()))
                            .orElse(Collections.emptyList());
                    return AdminSummaryDto.builder()
                            .id(admin.getId())
                            .name(admin.getName())
                            .email(admin.getEmail())
                            .role(admin.getRole())
                            .city(admin.getCity())
                            .assignedCities(assigned)
                            .active(admin.isActive())
                            .emailVerified(admin.isEmailVerified())
                            .createdAt(admin.getCreatedAt())
                            .build();
                })
                .toList();
    }

    public List<AppUser> getAllAdmins() {
        return appUserRepository.findByRole(UserRole.ADMIN);
    }

    // ✅ 3. Deactivate Admin
    public void deactivateAdmin(Long id) {

        AppUser admin = appUserRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Admin not found"
                ));

        if (admin.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User is not an admin"
            );
        }

        if (!admin.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Admin is already inactive"
            );
        }

        admin.setActive(false);
        appUserRepository.save(admin);
    }

    // ✅ 4. Reactivate Admin
    public void reactivateAdmin(Long id) {

        AppUser admin = appUserRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Admin not found"
                ));

        if (admin.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User is not an admin"
            );
        }

        if (admin.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Admin is already active"
            );
        }

        admin.setActive(true);
        appUserRepository.save(admin);
    }

    // ✅ 5. Delete Admin
    @Transactional
    public void deleteAdmin(Long id) {

        AppUser admin = appUserRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Admin not found"
                ));

        if (admin.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User is not an admin"
            );
        }

        // Unassign from any ServiceCity
        List<ServiceCity> cities = serviceCityRepository.findAllByAdmin_Id(admin.getId());
        for (ServiceCity city : cities) {
            city.setAdmin(null);
            serviceCityRepository.save(city);
        }

        // Unassign from any Car (currentAdmin & addedBy)
        List<Car> currentCars = carRepository.findByCurrentAdmin_Id(admin.getId());
        for (Car car : currentCars) {
            car.setCurrentAdmin(null);
            carRepository.save(car);
        }
        List<Car> addedCars = carRepository.findByAddedBy_Id(admin.getId());
        for (Car car : addedCars) {
            car.setAddedBy(null);
            carRepository.save(car);
        }

        // Unassign from any Booking
        List<Booking> bookings = bookingRepository.findByAssignedAdminId(admin.getId());
        for (Booking b : bookings) {
            b.setAssignedAdminId(null);
            bookingRepository.save(b);
        }

        userTokenRepository.deleteByUser(admin);
        appUserRepository.delete(admin);
        auditLogService.log("DELETE_ADMIN", "AppUser", String.valueOf(id), "Deleted admin: " + admin.getEmail());
    }

    // ✅ 6. Get All Users
    public List<AppUser> getAllUsers() {
        return appUserRepository.findByRole(UserRole.USER);
    }

    // ✅ 7. Deactivate User
    public void deactivateUser(Long id) {

        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        if (user.getRole() != UserRole.USER) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This account is not a user"
            );
        }

        if (!user.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User is already inactive"
            );
        }

        user.setActive(false);
        appUserRepository.save(user);
    }

    // ✅ 8. Reactivate User
    public void reactivateUser(Long id) {

        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        if (user.getRole() != UserRole.USER) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This account is not a user"
            );
        }

        if (user.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User is already active"
            );
        }

        user.setActive(true);
        appUserRepository.save(user);
    }

    // ✅ 9. Delete User (safe deletion guard)
    public void deleteUser(Long id) {

        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        if (user.getRole() != UserRole.USER) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This account is not a user"
            );
        }

        if (!bookingRepository.findByUser_Id(id).isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "User has existing bookings or transactions. Deactivate or suspend the account instead."
            );
        }

        userTokenRepository.deleteByUser(user);
        appUserRepository.delete(user);
        auditLogService.log("DELETE_USER", "AppUser", String.valueOf(id), "Deleted user: " + user.getEmail());
    }

    // ✅ 9b. Delete ALL Users (role=USER only)
    @Transactional
    public int deleteAllUsers() {
        List<AppUser> users = appUserRepository.findByRole(UserRole.USER);
        for (AppUser user : users) {
            bookingRepository.deleteByUser(user);
            driverProfileRepository.deleteByUser(user);
            userTokenRepository.deleteByUser(user);
        }
        appUserRepository.deleteAll(users);
        return users.size();
    }

    // ✅ 9c. Delete ALL Admins (role=ADMIN only)
    @Transactional
    public int deleteAllAdmins() {
        List<AppUser> admins = appUserRepository.findByRole(UserRole.ADMIN);
        for (AppUser admin : admins) {
            // Nullify admin in cities
            List<ServiceCity> cities = serviceCityRepository.findAll();
            for(ServiceCity city : cities) {
                if (city.getAdmin() != null && city.getAdmin().getId().equals(admin.getId())) {
                    city.setAdmin(null);
                    serviceCityRepository.save(city);
                }
            }
            // Bookings and Cars might have admin assignment. Nullify them.
            List<Car> cars = carRepository.findByCurrentAdmin_Id(admin.getId());
            for(Car car : cars) {
                car.setCurrentAdmin(null);
                carRepository.save(car);
            }
            List<Booking> bookings = bookingRepository.findByAssignedAdminId(admin.getId());
            for(Booking b : bookings) {
                b.setAssignedAdminId(null);
                bookingRepository.save(b);
            }
            userTokenRepository.deleteByUser(admin);
        }
        appUserRepository.deleteAll(admins);
        return admins.size();
    }

    // ✅ 9d. Delete ALL Drivers (role=DRIVER only)
    @Transactional
    public int deleteAllDrivers() {
        List<AppUser> drivers = appUserRepository.findByRole(UserRole.DRIVER);
        for (AppUser driver : drivers) {
            // For driver profile
            driverProfileRepository.findByUser(driver).ifPresent(dp -> {
                // Bookings might have this driverProfileId. We need to set it to null.
                List<Booking> bookings = bookingRepository.findAll(); // Simple approach, since we don't have findByDriverProfileId
                for(Booking b : bookings) {
                    if(b.getDriverProfile() != null && b.getDriverProfile().getId().equals(dp.getId())) {
                        b.setDriverProfile(null);
                        bookingRepository.save(b);
                    }
                }
            });
            driverProfileRepository.deleteByUser(driver);
            userTokenRepository.deleteByUser(driver);
        }
        appUserRepository.deleteAll(drivers);
        return drivers.size();
    }

    // ✅ 10. Get All Drivers
    public List<DriverProfile> getAllDrivers() {          // 👈 FIXED
        return driverProfileRepository.findAll();
    }

    // ✅ 11. Deactivate Driver
    public void deactivateDriver(Long userId) {

        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Driver not found"
                ));

        if (user.getRole() != UserRole.DRIVER) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This account is not a driver"
            );
        }

        DriverProfile profile = driverProfileRepository  // 👈 FIXED
                .findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Driver profile not found"
                ));

        if (!user.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Driver is already inactive"
            );
        }

        user.setActive(false);
        profile.setActive(false);
        profile.setStatus(DriverStatus.INACTIVE);

        appUserRepository.save(user);
        driverProfileRepository.save(profile);
    }

    // ✅ 12. Reactivate Driver
    public void reactivateDriver(Long userId) {

        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Driver not found"
                ));

        if (user.getRole() != UserRole.DRIVER) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This account is not a driver"
            );
        }

        DriverProfile profile = driverProfileRepository  // 👈 FIXED
                .findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Driver profile not found"
                ));

        if (user.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Driver is already active"
            );
        }

        user.setActive(true);
        profile.setActive(true);
        profile.setStatus(DriverStatus.AVAILABLE);

        appUserRepository.save(user);
        driverProfileRepository.save(profile);
    }
    // ✅ 13. Add New City
    public ServiceCity addCity(ServiceCityRequest req) {

        // Step 1 - validate city name
        if (req.getCityName() == null || req.getCityName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "City name is required");
        }

        // Step 2 - check duplicate (case-insensitive)
        if (serviceCityRepository.findByCityNameIgnoreCase(req.getCityName().trim()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "City already exists: " + req.getCityName());
        }

        // Step 3 - find admin if provided
        AppUser admin = null;
        if (req.getAdminId() != null) {
            admin = appUserRepository.findById(req.getAdminId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Admin not found"));

            // verify it's actually an admin
            if (admin.getRole() != UserRole.ADMIN) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "User is not an admin");
            }

            // verify admin's city matches
            if (admin.getCity() == null || !admin.getCity().equalsIgnoreCase(req.getCityName())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Admin's city does not match: "
                                + admin.getCity());
            }
        }

        // Step 4 - build service city
        ServiceCity city = ServiceCity.builder()
                .cityName(req.getCityName().trim())
                .parkingAddress(req.getParkingAddress())
                .parkingContact(req.getParkingContact())
                .oneWayFee(req.getOneWayFee() != null
                        ? req.getOneWayFee()
                        : 500.0)                // default ₹500
                .active(true)
                .admin(admin)
                .build();

        ServiceCity savedCity = serviceCityRepository.save(city);

        if (admin != null) {
            emailService.sendEmail(
                    admin.getEmail(),
                    "City Management Assignment",
                    "Hello " + admin.getName() + ",\n\n" +
                            "A new service city has been created and you have been assigned to manage it:\n" +
                            "City: " + savedCity.getCityName() + "\n" +
                            "Parking Address: " + savedCity.getParkingAddress() + "\n" +
                            "Parking Contact: " + savedCity.getParkingContact() + "\n\n" +
                            "Best regards,\nCar Rental Team"
            );
        }

        return savedCity;
    }

    // ✅ 14. Get All Cities
    public List<ServiceCity> getAllCities() {
        return serviceCityRepository.findAll();
    }

    // ✅ 15. Get Active Cities Only
    public List<ServiceCity> getActiveCities() {
        return serviceCityRepository.findByActiveTrue();
    }

    // ✅ 16. Deactivate City
    public void deactivateCity(Long id) {

        ServiceCity city = serviceCityRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "City not found"));

        if (!city.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "City is already inactive");
        }

        city.setActive(false);
        serviceCityRepository.save(city);
    }

    // ✅ 17. Reactivate City
    public void reactivateCity(Long id) {

        ServiceCity city = serviceCityRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "City not found"));

        if (city.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "City is already active");
        }

        city.setActive(true);
        serviceCityRepository.save(city);
    }

    // ✅ 18. Assign Admin to City
    public ServiceCity assignAdminToCity(Long cityId, Long adminId) {

        // find city
        ServiceCity city = serviceCityRepository.findById(cityId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "City not found"));

        // find admin
        AppUser admin = appUserRepository.findById(adminId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Admin not found"));

        // verify role
        if (admin.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User is not an admin");
        }

        // verify city matches
        if (admin.getCity() == null || !admin.getCity().equalsIgnoreCase(city.getCityName())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Admin's city " + admin.getCity()
                            + " does not match service city "
                            + city.getCityName());
        }

        city.setAdmin(admin);
        ServiceCity savedCity = serviceCityRepository.save(city);

        emailService.sendEmail(
                admin.getEmail(),
                "City Management Assignment",
                "Hello " + admin.getName() + ",\n\n" +
                        "You have been assigned to manage the city: " + savedCity.getCityName() + ".\n" +
                        "Parking Address: " + savedCity.getParkingAddress() + "\n" +
                        "Parking Contact: " + savedCity.getParkingContact() + "\n\n" +
                        "Best regards,\nCar Rental Team"
        );

        return savedCity;
    }

    // ✅ 19. Update One Way Fee
    public ServiceCity updateOneWayFee(Long cityId, double newFee) {

        if (newFee < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Fee cannot be negative");
        }

        ServiceCity city = serviceCityRepository.findById(cityId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "City not found"));

        city.setOneWayFee(newFee);
        return serviceCityRepository.save(city);
    }
    // ✅ 20. Create Car Pricing
    @Transactional
    public CarPricing createCarPricing(CarPricingRequest req) {

        if (req.getCarClass() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Car class is required");
        }

        boolean isActive = req.getActive() == null || Boolean.TRUE.equals(req.getActive());
        if (isActive && carPricingRepository.existsByCarClassAndActiveTrue(req.getCarClass())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Active pricing configuration already exists for: " + req.getCarClass());
        }

        if (req.getMinPerDayRate() > req.getMaxPerDayRate()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Minimum daily price cannot be greater than maximum daily price");
        }

        if (req.getPerDayRate() < req.getMinPerDayRate() ||
                req.getPerDayRate() > req.getMaxPerDayRate()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Default rate must be between minimum and maximum daily price");
        }

        if (req.getMinBookingDurationDays() != null && req.getMaxBookingDurationDays() != null
                && req.getMinBookingDurationDays() > req.getMaxBookingDurationDays()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Minimum booking duration cannot exceed maximum booking duration");
        }

        if (req.getMinPlatformFee() != null && req.getMaxPlatformFee() != null
                && req.getMaxPlatformFee().compareTo(BigDecimal.ZERO) > 0
                && req.getMinPlatformFee().compareTo(req.getMaxPlatformFee()) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Minimum platform fee cannot exceed maximum platform fee");
        }

        CarPricing pricing = CarPricing.builder()
                .carClass(req.getCarClass())
                .baseFare(req.getBaseFare())
                .perDayRate(req.getPerDayRate())
                .minPerDayRate(req.getMinPerDayRate())
                .maxPerDayRate(req.getMaxPerDayRate())
                .freeKmPerDay(req.getFreeKmPerDay())
                .extraKmRate(req.getExtraKmRate())
                .platformCommissionPercent(req.getPlatformCommissionPercent() != null
                        ? req.getPlatformCommissionPercent() : new BigDecimal("20.00"))
                .minPlatformFee(req.getMinPlatformFee() != null ? req.getMinPlatformFee() : BigDecimal.ZERO)
                .maxPlatformFee(req.getMaxPlatformFee())
                .minBookingDurationDays(req.getMinBookingDurationDays() != null ? req.getMinBookingDurationDays() : 1)
                .maxBookingDurationDays(req.getMaxBookingDurationDays() != null ? req.getMaxBookingDurationDays() : 30)
                .active(isActive)
                .build();

        CarPricing saved = carPricingRepository.save(pricing);
        if (saved.isActiveRule()) {
            pricingValidationService.flagOutOfRangeVehicles(saved);
        }
        auditLogService.log("CREATE_PRICING", "CarPricing", String.valueOf(saved.getId()),
                "Created pricing for " + saved.getCarClass() + " [" + saved.getMinPerDayRate() + "-" + saved.getMaxPerDayRate() + "]");
        return saved;
    }

    // ✅ 21. Get All Pricing Rules
    public List<CarPricing> getAllPricing() {
        return carPricingRepository.findAll();
    }

    // ✅ 22. Get Pricing by CarClass
    public CarPricing getPricingByClass(CarClass carClass) {
        return carPricingRepository.findFirstByCarClassAndActiveTrue(carClass)
                .or(() -> carPricingRepository.findByCarClass(carClass))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No pricing found for: " + carClass));
    }

    // ✅ 23. Update Car Pricing
    @Transactional
    public CarPricing updateCarPricing(Long id, CarPricingRequest req) {

        CarPricing pricing = carPricingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pricing not found"));

        if (req.getBaseFare() >= 0) pricing.setBaseFare(req.getBaseFare());
        if (req.getPerDayRate() > 0) pricing.setPerDayRate(req.getPerDayRate());
        if (req.getMinPerDayRate() > 0) pricing.setMinPerDayRate(req.getMinPerDayRate());
        if (req.getMaxPerDayRate() > 0) pricing.setMaxPerDayRate(req.getMaxPerDayRate());
        if (req.getFreeKmPerDay() >= 0) pricing.setFreeKmPerDay(req.getFreeKmPerDay());
        if (req.getExtraKmRate() >= 0) pricing.setExtraKmRate(req.getExtraKmRate());
        if (req.getPlatformCommissionPercent() != null) {
            pricing.setPlatformCommissionPercent(req.getPlatformCommissionPercent());
        }
        if (req.getMinPlatformFee() != null) {
            pricing.setMinPlatformFee(req.getMinPlatformFee());
        }
        pricing.setMaxPlatformFee(req.getMaxPlatformFee());
        if (req.getMinBookingDurationDays() != null) {
            pricing.setMinBookingDurationDays(req.getMinBookingDurationDays());
        }
        if (req.getMaxBookingDurationDays() != null) {
            pricing.setMaxBookingDurationDays(req.getMaxBookingDurationDays());
        }
        if (req.getActive() != null) {
            if (Boolean.TRUE.equals(req.getActive())
                    && carPricingRepository.existsByCarClassAndActiveTrueAndIdNot(pricing.getCarClass(), id)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Another active pricing configuration already exists for " + pricing.getCarClass());
            }
            pricing.setActive(req.getActive());
        }

        if (pricing.getMinPerDayRate() > pricing.getMaxPerDayRate()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Minimum daily price cannot be greater than maximum daily price");
        }

        if (pricing.getPerDayRate() < pricing.getMinPerDayRate() ||
                pricing.getPerDayRate() > pricing.getMaxPerDayRate()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Default rate must be between minimum and maximum daily price");
        }

        if (pricing.getMinBookingDurationDays() != null && pricing.getMaxBookingDurationDays() != null
                && pricing.getMinBookingDurationDays() > pricing.getMaxBookingDurationDays()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Minimum booking duration cannot exceed maximum booking duration");
        }

        CarPricing saved = carPricingRepository.save(pricing);
        if (saved.isActiveRule()) {
            pricingValidationService.flagOutOfRangeVehicles(saved);
        }
        auditLogService.log("UPDATE_PRICING", "CarPricing", String.valueOf(saved.getId()),
                "Updated pricing for " + saved.getCarClass() + " [" + saved.getMinPerDayRate() + "-" + saved.getMaxPerDayRate() + "]");
        return saved;
    }

    @Transactional
    public CarPricing setPricingActiveStatus(Long id, boolean active) {
        CarPricing pricing = carPricingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pricing not found"));
        if (active && carPricingRepository.existsByCarClassAndActiveTrueAndIdNot(pricing.getCarClass(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Another active pricing configuration already exists for " + pricing.getCarClass());
        }
        pricing.setActive(active);
        CarPricing saved = carPricingRepository.save(pricing);
        if (active) {
            pricingValidationService.flagOutOfRangeVehicles(saved);
        }
        auditLogService.log(active ? "ACTIVATE_PRICING" : "DEACTIVATE_PRICING", "CarPricing", String.valueOf(id),
                "CarClass=" + pricing.getCarClass());
        return saved;
    }

    // ✅ 24. Delete Car Pricing
    public void deleteCarPricing(Long id) {

        CarPricing pricing = carPricingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pricing not found"));

        carPricingRepository.delete(pricing);
    }
    // ✅ 25. Get All Cars Globally
    public List<Car> getAllCars() {
        return carRepository.findAll();
    }

    // ✅ 26. Get Cars by Status
    public List<Car> getCarsByStatus(CarStatus status) {
        return carRepository.findByStatus(status);
    }

    // ✅ 27. Get Cars by City
    public List<Car> getCarsByCity(String city) {
        return carRepository.findByCurrentCityAndActiveTrue(city);
    }

    // ✅ 28. Get Available Cars by City
    public List<Car> getAvailableCarsByCity(String city) {
        return carRepository.findByCurrentCityAndStatusAndActiveTrue(
                city,
                CarStatus.AVAILABLE);
    }

    // ✅ 29. Get Cars by Admin
    public List<Car> getCarsByAdmin(Long adminId) {

        // verify admin exists
        appUserRepository.findById(adminId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Admin not found"));

        return carRepository.findByCurrentAdmin_Id(adminId);
    }

    // ✅ 30. Get Cars Needing Repositioning
    public List<Car> getCarsNeedingRepositioning() {
        return carRepository.findByStatus(
                CarStatus.NEEDS_REPOSITIONING);
    }

    public DashboardStatsResponse getDashboardStats() {

        long totalAdmins = appUserRepository.countByRole(UserRole.ADMIN);
        long totalUsers = appUserRepository.countByRole(UserRole.USER);
        long totalHosts = hostProfileRepository.count();
        long totalCities = serviceCityRepository.count();
        long totalListedCars = carRepository.count();
        long totalBookings = bookingRepository.count();

        BigDecimal totalRevenue = bookingRepository.getTotalRevenue();
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        BigDecimal platformEarnings = bookingRepository.getTotalPlatformEarnings();
        if (platformEarnings == null) platformEarnings = BigDecimal.ZERO;

        long pendingHostVerifications = hostProfileRepository.countByVerificationStatus(HostVerificationStatus.PENDING);
        long pendingVehicleApprovals = carRepository.countByApprovalStatus(CarApprovalStatus.PENDING);
        long activeBookings = bookingRepository.countActiveBookings();
        long availableCars = carRepository.countByStatusAndActiveTrue(CarStatus.AVAILABLE);
        long unavailableCars = Math.max(0, totalListedCars - availableCars);
        long pendingHostPayouts = hostPayoutRepository.countByStatus(PayoutStatus.PENDING);
        BigDecimal pendingHostPayoutAmount = hostPayoutRepository.sumPendingPayoutAmount();
        if (pendingHostPayoutAmount == null) pendingHostPayoutAmount = BigDecimal.ZERO;

        return DashboardStatsResponse.builder()
                .totalAdmins(totalAdmins)
                .totalUsers(totalUsers)
                .totalHosts(totalHosts)
                .totalCities(totalCities)
                .totalListedCars(totalListedCars)
                .totalBookings(totalBookings)
                .totalRevenue(totalRevenue)
                .platformEarnings(platformEarnings)
                .pendingHostVerifications(pendingHostVerifications)
                .pendingVehicleApprovals(pendingVehicleApprovals)
                .activeBookings(activeBookings)
                .availableCars(availableCars)
                .unavailableCars(unavailableCars)
                .pendingHostPayouts(pendingHostPayouts)
                .pendingHostPayoutAmount(pendingHostPayoutAmount)
                .build();
    }
}