package carRental.CarRrental.Services;

import carRental.CarRrental.Dtos.CarCreateRequest;
import carRental.CarRrental.Dtos.CarUpdateRequest;
import carRental.CarRrental.Models.Car;
import carRental.CarRrental.Repositories.CarRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminCarService {

    private final CarRepository carRepository;

    public AdminCarService(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    public Car addCar(CarCreateRequest req) {
        String carNumber = req.getCarNumber().trim().toUpperCase();

        if (carRepository.existsByCarNumber(carNumber)) {
            throw new RuntimeException("Car already exists with number: " + carNumber);
        }

        Car car = Car.builder()
                .carNumber(carNumber)
                .brand(req.getBrand())
                .model(req.getModel())
                .carClass(req.getCarClass())
                .fuelType(req.getFuelType())
                .transmission(req.getTransmission())
                .pricePerDay(req.getPricePerDay())
                .currentLocation(req.getCurrentLocation())
                .active(true)
                .build();

        return carRepository.save(car);
    }

    public List<Car> getAllCars() {
        return carRepository.findAll();
    }

    public Car getCarById(Long id) {
        return carRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Car not found: " + id));
    }

    public Car updateCar(Long id, CarUpdateRequest req) {
        Car car = getCarById(id);

        if (req.getBrand() != null) car.setBrand(req.getBrand());
        if (req.getModel() != null) car.setModel(req.getModel());
        if (req.getCarClass() != null) car.setCarClass(req.getCarClass());
        if (req.getFuelType() != null) car.setFuelType(req.getFuelType());
        if (req.getTransmission() != null) car.setTransmission(req.getTransmission());
        if (req.getPricePerDay() != null) car.setPricePerDay(req.getPricePerDay());
        if (req.getCurrentLocation() != null) car.setCurrentLocation(req.getCurrentLocation());
        if (req.getActive() != null) car.setActive(req.getActive());

        return carRepository.save(car);
    }

    public void disableCar(Long id) {
        Car car = getCarById(id);
        car.setActive(false);
        carRepository.save(car);
    }
}
