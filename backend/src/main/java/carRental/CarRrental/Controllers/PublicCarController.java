package carRental.CarRrental.Controllers;

import carRental.CarRrental.Models.Car;
import carRental.CarRrental.Models.CarStatus;
import carRental.CarRrental.Repositories.CarRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cars")
public class PublicCarController {

    private final CarRepository carRepository;

    public PublicCarController(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    /**
     * Returns all AVAILABLE, active cars - optionally filtered by city.
     * Accessible by authenticated users.
     */
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/available")
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
}
