package carRental.CarRrental.Controllers;

import carRental.CarRrental.Dtos.CarCreateRequest;
import carRental.CarRrental.Dtos.CarUpdateRequest;
import carRental.CarRrental.Models.Car;
import carRental.CarRrental.Services.AdminCarService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/cars")
public class AdminCarController {

    private final AdminCarService adminCarService;

    public AdminCarController(AdminCarService adminCarService) {
        this.adminCarService = adminCarService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PostMapping
    public Car addCar(@RequestBody CarCreateRequest req) {
        return adminCarService.addCar(req);
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @GetMapping
    public List<Car> getAll() {
        return adminCarService.getAllCars();
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @GetMapping("/{id}")
    public Car getById(@PathVariable Long id) {
        return adminCarService.getCarById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PutMapping("/{id}")
    public Car update(@PathVariable Long id, @RequestBody CarUpdateRequest req) {
        return adminCarService.updateCar(id, req);
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public String disable(@PathVariable Long id) {
        adminCarService.disableCar(id);
        return "Car disabled";
    }
}
