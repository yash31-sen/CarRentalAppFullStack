package carRental.CarRrental.Controllers;

import carRental.CarRrental.Dtos.DriverCreateRequest;
import carRental.CarRrental.Dtos.DriverUpdateRequest;
import carRental.CarRrental.Models.DriverProfile;        // 👈 FIXED
import carRental.CarRrental.Services.AdminDriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/drivers")
@RequiredArgsConstructor
@EnableMethodSecurity
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminDriverController {

    private final AdminDriverService adminDriverService;

    @PostMapping
    public ResponseEntity<DriverProfile> add(
            @RequestBody DriverCreateRequest req) {
        return ResponseEntity.status(201)
                .body(adminDriverService.addDriver(req));
    }

    @GetMapping
    public ResponseEntity<List<DriverProfile>> all() {
        return ResponseEntity.ok(
                adminDriverService.getAllDrivers());
    }

    @PutMapping("/{id}")
    public ResponseEntity<DriverProfile> update(
            @PathVariable Long id,
            @RequestBody DriverUpdateRequest req) {
        return ResponseEntity.ok(
                adminDriverService.updateDriver(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> disable(
            @PathVariable Long id) {
        adminDriverService.disableDriver(id);
        return ResponseEntity.ok("Driver disabled");
    }
}