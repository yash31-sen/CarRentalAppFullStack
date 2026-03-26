package carRental.CarRrental.Controllers;

import carRental.CarRrental.Dtos.DriverCreateRequest;
import carRental.CarRrental.Dtos.DriverUpdateRequest;
import carRental.CarRrental.Models.DriverProfile;
import carRental.CarRrental.Services.AdminDriverService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/admin/drivers")
public class AdminDriverController {

    private final AdminDriverService adminDriverService;

    public AdminDriverController(AdminDriverService adminDriverService) {
        this.adminDriverService = adminDriverService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PostMapping
    public DriverProfile add(@RequestBody DriverCreateRequest req) {
        return adminDriverService.addDriver(req);
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @GetMapping
    public List<DriverProfile> all() {
        return adminDriverService.getAllDrivers();
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PutMapping("/{id}")
    public DriverProfile update(@PathVariable Long id, @RequestBody DriverUpdateRequest req) {
        return adminDriverService.updateDriver(id, req);
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public String disable(@PathVariable Long id) {
        adminDriverService.disableDriver(id);
        return "Driver disabled";
    }
}
