package carRental.CarRrental.Services;

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
import org.springframework.web.server.ResponseStatusException;


import java.util.List;

@Service
@RequiredArgsConstructor
public class SuperAdminService {
    // add this dependency at top
    private final CarPricingRepository carPricingRepository;
    private final ServiceCityRepository serviceCityRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserTokenRepository userTokenRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final CarRepository carRepository;

private   final   BookingRepository bookingRepository;
    // ✅ 1. Create Admin
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

        AppUser admin = AppUser.builder()
                .name(req.getName())
                .email(email)
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role(UserRole.ADMIN)
                .emailVerified(true)
                .isActive(true)
                .city(req.getCity().trim())
                .build();

        appUserRepository.save(admin);
        return true;
    }

    // ✅ 2. Get All Admins
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

        userTokenRepository.deleteByUser(admin);
        appUserRepository.delete(admin);
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

    // ✅ 9. Delete User
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

        userTokenRepository.deleteByUser(user);
        appUserRepository.delete(user);
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

        // Step 2 - check duplicate
        if (serviceCityRepository.existsByCityNameAndActiveTrue(
                req.getCityName())) {
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
            if (!admin.getCity().equalsIgnoreCase(req.getCityName())) {
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

        return serviceCityRepository.save(city);
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
        if (!admin.getCity().equalsIgnoreCase(city.getCityName())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Admin's city " + admin.getCity()
                            + " does not match service city "
                            + city.getCityName());
        }

        city.setAdmin(admin);
        return serviceCityRepository.save(city);
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
    public CarPricing createCarPricing(CarPricingRequest req) {

        // Step 1 - validate carClass provided
        if (req.getCarClass() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Car class is required");
        }

        // Step 2 - check duplicate
        if (carPricingRepository.existsByCarClass(req.getCarClass())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Pricing already exists for: "
                            + req.getCarClass());
        }

        // Step 3 - validate price range
        if (req.getMinPerDayRate() > req.getMaxPerDayRate()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Min rate cannot be greater than max rate");
        }

        if (req.getPerDayRate() < req.getMinPerDayRate() ||
                req.getPerDayRate() > req.getMaxPerDayRate()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Default rate must be between min and max rate");
        }

        // Step 4 - validate km fields
        if (req.getFreeKmPerDay() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Free km cannot be negative");
        }

        if (req.getExtraKmRate() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Extra km rate cannot be negative");
        }

        // Step 5 - build pricing
        CarPricing pricing = CarPricing.builder()
                .carClass(req.getCarClass())
                .baseFare(req.getBaseFare())
                .perDayRate(req.getPerDayRate())
                .minPerDayRate(req.getMinPerDayRate())
                .maxPerDayRate(req.getMaxPerDayRate())
                .freeKmPerDay(req.getFreeKmPerDay())
                .extraKmRate(req.getExtraKmRate())
                .build();

        return carPricingRepository.save(pricing);
    }

    // ✅ 21. Get All Pricing Rules
    public List<CarPricing> getAllPricing() {
        return carPricingRepository.findAll();
    }

    // ✅ 22. Get Pricing by CarClass
    public CarPricing getPricingByClass(CarClass carClass) {
        return carPricingRepository.findByCarClass(carClass)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No pricing found for: " + carClass));
    }

    // ✅ 23. Update Car Pricing
    public CarPricing updateCarPricing(
            Long id, CarPricingRequest req) {

        CarPricing pricing = carPricingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pricing not found"));

        // update only provided fields
        if (req.getBaseFare() > 0)
            pricing.setBaseFare(req.getBaseFare());

        if (req.getPerDayRate() > 0)
            pricing.setPerDayRate(req.getPerDayRate());

        if (req.getMinPerDayRate() > 0)
            pricing.setMinPerDayRate(req.getMinPerDayRate());

        if (req.getMaxPerDayRate() > 0)
            pricing.setMaxPerDayRate(req.getMaxPerDayRate());

        if (req.getFreeKmPerDay() > 0)
            pricing.setFreeKmPerDay(req.getFreeKmPerDay());

        if (req.getExtraKmRate() > 0)
            pricing.setExtraKmRate(req.getExtraKmRate());

        // re-validate range after update
        if (pricing.getMinPerDayRate() >
                pricing.getMaxPerDayRate()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Min rate cannot be greater than max rate");
        }

        if (pricing.getPerDayRate() < pricing.getMinPerDayRate() ||
                pricing.getPerDayRate() > pricing.getMaxPerDayRate()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Default rate must be between min and max");
        }

        return carPricingRepository.save(pricing);
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

        long totalAdmins =
                appUserRepository.countByRole(UserRole.ADMIN);

        long totalUsers =
                appUserRepository.countByRole(UserRole.USER);

        long totalDrivers =
                driverProfileRepository.count();

        long totalCities =
                serviceCityRepository.count();

        long totalCars =
                carRepository.count();

        long totalBookings =
                bookingRepository.count();

//        double totalRevenue =
//                bookingRepository.getTotalRevenue();

        return DashboardStatsResponse.builder()
                .totalAdmins(totalAdmins)
                .totalUsers(totalUsers)
                .totalDrivers(totalDrivers)
                .totalCities(totalCities)
                .totalCars(totalCars)
                .totalBookings(totalBookings)
//                .totalRevenue(totalRevenue)
                .build();
    }
}