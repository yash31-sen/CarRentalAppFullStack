package carRental.CarRrental.Services;

import carRental.CarRrental.Dtos.CarCreateRequest;
import carRental.CarRrental.Dtos.CarUpdateRequest;
import carRental.CarRrental.Models.*;
import carRental.CarRrental.Repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminCarService {

    private final CarRepository carRepository;
    private final AppUserRepository userRepository;           // 👈 NEW
    private final ServiceCityRepository serviceCityRepository; // 👈 NEW

    // =============================================
    // 1. ADD CAR
    // =============================================
    public Car addCar(CarCreateRequest req, String adminEmail) {

        // Step 1 - normalize car number
        String carNumber = req.getCarNumber().trim().toUpperCase();

        // Step 2 - check duplicate
        if (carRepository.existsByCarNumber(carNumber)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Car already exists with number: " + carNumber);
        }

        // Step 3 - find admin
        AppUser admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Admin not found"));

        // Step 4 - validate city
        // admin's city must be a registered ServiceCity
        String city = admin.getCity();

        if (city == null || city.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Admin has no city assigned");
        }

        boolean cityExists = serviceCityRepository
                .existsByCityNameAndActiveTrue(city);

        if (!cityExists) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Admin's city is not a registered service city");
        }

        // Step 5 - get parking address from service city
        ServiceCity serviceCity = serviceCityRepository
                .findByCityName(city)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Service city not found"));

        // Step 6 - build car
        Car car = Car.builder()
                .carNumber(carNumber)
                .brand(req.getBrand())
                .model(req.getModel())
                .carClass(req.getCarClass())
                .fuelType(req.getFuelType())
                .transmission(req.getTransmission())
                .pricePerDay(req.getPricePerDay())
                .homeCity(city)                          // 👈 NEW
                .currentCity(city)                       // 👈 NEW
                .currentLocation(serviceCity             // 👈 NEW
                        .getParkingAddress())
                .status(CarStatus.AVAILABLE)             // 👈 NEW
                .active(true)
                .addedBy(admin)                          // 👈 NEW
                .currentAdmin(admin)                     // 👈 NEW
                .build();

        return carRepository.save(car);
    }

    // =============================================
    // 2. GET ALL CARS
    // =============================================
    public List<Car> getAllCars() {
        return carRepository.findAll();
    }

    // =============================================
    // 3. GET CAR BY ID
    // =============================================
    public Car getCarById(Long id) {
        return carRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Car not found: " + id));
    }

    // =============================================
    // 4. UPDATE CAR
    // =============================================
    public Car updateCar(Long id, CarUpdateRequest req) {

        Car car = getCarById(id);

        if (req.getBrand() != null)
            car.setBrand(req.getBrand());

        if (req.getModel() != null)
            car.setModel(req.getModel());

        if (req.getCarClass() != null)
            car.setCarClass(req.getCarClass());

        if (req.getFuelType() != null)
            car.setFuelType(req.getFuelType());

        if (req.getTransmission() != null)
            car.setTransmission(req.getTransmission());

        if (req.getPricePerDay() != null)
            car.setPricePerDay(req.getPricePerDay());

        // 👇 NEW - only allow location update
        // if car is not on a trip
        if (req.getCurrentLocation() != null) {
            if (car.getStatus() == CarStatus.BOOKED) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Cannot update location while car is on a trip");
            }
            car.setCurrentLocation(req.getCurrentLocation());
        }

        // 👇 NEW - only allow status update
        // if car is not booked
        if (req.getStatus() != null) {
            if (car.getStatus() == CarStatus.BOOKED) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Cannot update status while car is booked");
            }
            car.setStatus(req.getStatus());
        }

        if (req.getActive() != null)
            car.setActive(req.getActive());

        return carRepository.save(car);
    }

    // =============================================
    // 5. DISABLE CAR
    // =============================================
    public void disableCar(Long id) {

        Car car = getCarById(id);

        // cannot disable car that is on a trip
        if (car.getStatus() == CarStatus.BOOKED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot disable car while it is on a trip");
        }

        car.setActive(false);
        car.setStatus(CarStatus.DISABLED);
        carRepository.save(car);
    }

    // =============================================
    // 6. GET CARS BY STATUS
    // =============================================
    public List<Car> getCarsByStatus(CarStatus status) {
        return carRepository.findByStatus(status);
    }

    // =============================================
    // 7. GET CARS BY CITY
    // =============================================
    public List<Car> getCarsByCity(String city) {
        return carRepository.findByCurrentCityAndActiveTrue(city);
    }
}