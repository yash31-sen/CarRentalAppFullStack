package carRental.CarRrental.Controllers;

import carRental.CarRrental.Models.Car;
import carRental.CarRrental.Models.CarStatus;
import carRental.CarRrental.Models.ServiceCity;
import carRental.CarRrental.Repositories.CarRepository;
import carRental.CarRrental.Repositories.ServiceCityRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PublicCarController {

    private final CarRepository carRepository;
    private final ServiceCityRepository serviceCityRepository;

    public PublicCarController(CarRepository carRepository, ServiceCityRepository serviceCityRepository) {
        this.carRepository = carRepository;
        this.serviceCityRepository = serviceCityRepository;
    }

    /**
     * Returns all AVAILABLE, active cars - optionally filtered by city.
     * Accessible by authenticated users.
     */
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/cars/available")
    public List<Car> getAvailableCars(
            @RequestParam(required = false) String city) {

        if (city != null && !city.isBlank()) {
            return carRepository.findByCurrentCityAndStatusAndActiveTrue(
                    city, CarStatus.AVAILABLE);
        }
        return carRepository.findAll().stream()
                .filter(c -> c.isActive() && c.getStatus() == CarStatus.AVAILABLE)
                .toList();
    }

    /**
     * Returns list of unique active city names for user booking flow.
     */
    @GetMapping("/cities")
    public List<String> getAvailableCities() {
        return serviceCityRepository.findAll().stream()
                .filter(ServiceCity::isActive)
                .map(ServiceCity::getCityName)
                .distinct()
                .sorted()
                .toList();
    }
}
