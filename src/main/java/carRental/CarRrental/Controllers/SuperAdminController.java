package carRental.CarRrental.Controllers;

import carRental.CarRrental.Dtos.CarPricingRequest;
import carRental.CarRrental.Dtos.CreateAdminRequest;
import carRental.CarRrental.Dtos.DashboardStatsResponse;
import carRental.CarRrental.Dtos.ServiceCityRequest;
import carRental.CarRrental.Models.*;
import carRental.CarRrental.Services.SuperAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/super-admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SuperAdminController {

    private final SuperAdminService superAdminService;

    // ================= ADMINS =================

    @PostMapping("/admins")
    public ResponseEntity<Map<String, String>> createAdmin(
            @RequestBody CreateAdminRequest req
    ) {

        boolean created = superAdminService.createAdmin(req);

        if (!created) {

            return ResponseEntity.status(409).body(
                    Map.of(
                            "message",
                            "Email already exists"
                    )
            );
        }

        return ResponseEntity.status(201).body(
                Map.of(
                        "message",
                        "Admin created successfully"
                )
        );
    }

    @GetMapping("/admins")
    public ResponseEntity<List<AppUser>> getAllAdmins() {

        return ResponseEntity.ok(
                superAdminService.getAllAdmins()
        );
    }

    @PatchMapping("/admins/{id}/deactivate")
    public ResponseEntity<Map<String, String>> deactivateAdmin(
            @PathVariable Long id
    ) {

        superAdminService.deactivateAdmin(id);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Admin deactivated successfully"
                )
        );
    }

    @PatchMapping("/admins/{id}/reactivate")
    public ResponseEntity<Map<String, String>> reactivateAdmin(
            @PathVariable Long id
    ) {

        superAdminService.reactivateAdmin(id);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Admin reactivated successfully"
                )
        );
    }

    @DeleteMapping("/admins/{id}")
    public ResponseEntity<Map<String, String>> deleteAdmin(
            @PathVariable Long id
    ) {

        superAdminService.deleteAdmin(id);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Admin deleted successfully"
                )
        );
    }

    // ================= USERS =================

    @GetMapping("/users")
    public ResponseEntity<List<AppUser>> getAllUsers() {

        return ResponseEntity.ok(
                superAdminService.getAllUsers()
        );
    }

    @PatchMapping("/users/{id}/deactivate")
    public ResponseEntity<Map<String, String>> deactivateUser(
            @PathVariable Long id
    ) {

        superAdminService.deactivateUser(id);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "User deactivated successfully"
                )
        );
    }

    @PatchMapping("/users/{id}/reactivate")
    public ResponseEntity<Map<String, String>> reactivateUser(
            @PathVariable Long id
    ) {

        superAdminService.reactivateUser(id);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "User reactivated successfully"
                )
        );
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(
            @PathVariable Long id
    ) {

        superAdminService.deleteUser(id);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "User deleted successfully"
                )
        );
    }

    // ================= DRIVERS =================

    @GetMapping("/drivers")
    public ResponseEntity<List<DriverProfile>> getAllDrivers() {

        return ResponseEntity.ok(
                superAdminService.getAllDrivers()
        );
    }

    @PatchMapping("/drivers/{userId}/deactivate")
    public ResponseEntity<Map<String, String>> deactivateDriver(
            @PathVariable Long userId
    ) {

        superAdminService.deactivateDriver(userId);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Driver deactivated successfully"
                )
        );
    }

    @PatchMapping("/drivers/{userId}/reactivate")
    public ResponseEntity<Map<String, String>> reactivateDriver(
            @PathVariable Long userId
    ) {

        superAdminService.reactivateDriver(userId);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Driver reactivated successfully"
                )
        );
    }

    // ================= CITIES =================

    @PostMapping("/cities")
    public ResponseEntity<?> addCity(
            @RequestBody ServiceCityRequest req
    ) {

        try {

            ServiceCity city = superAdminService.addCity(req);

            return ResponseEntity.status(201).body(city);

        } catch (Exception e) {

            return ResponseEntity.status(409).body(
                    Map.of(
                            "message",
                            e.getMessage()
                    )
            );
        }
    }

    @GetMapping("/cities")
    public ResponseEntity<List<ServiceCity>> getAllCities() {

        return ResponseEntity.ok(
                superAdminService.getAllCities()
        );
    }

    @GetMapping("/cities/active")
    public ResponseEntity<List<ServiceCity>> getActiveCities() {

        return ResponseEntity.ok(
                superAdminService.getActiveCities()
        );
    }

    @PatchMapping("/cities/{id}/deactivate")
    public ResponseEntity<Map<String, String>> deactivateCity(
            @PathVariable Long id
    ) {

        superAdminService.deactivateCity(id);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "City deactivated successfully"
                )
        );
    }

    @PatchMapping("/cities/{id}/reactivate")
    public ResponseEntity<Map<String, String>> reactivateCity(
            @PathVariable Long id
    ) {

        superAdminService.reactivateCity(id);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "City reactivated successfully"
                )
        );
    }

    @PatchMapping("/cities/{cityId}/assign-admin/{adminId}")
    public ResponseEntity<ServiceCity> assignAdminToCity(
            @PathVariable Long cityId,
            @PathVariable Long adminId
    ) {

        return ResponseEntity.ok(
                superAdminService.assignAdminToCity(cityId, adminId)
        );
    }

    @PatchMapping("/cities/{id}/one-way-fee")
    public ResponseEntity<ServiceCity> updateOneWayFee(
            @PathVariable Long id,
            @RequestParam double fee
    ) {

        return ResponseEntity.ok(
                superAdminService.updateOneWayFee(id, fee)
        );
    }

    // ================= PRICING =================

    @PostMapping("/pricing")
    public ResponseEntity<?> createPricing(
            @RequestBody CarPricingRequest req
    ) {

        try {

            return ResponseEntity.status(201).body(
                    superAdminService.createCarPricing(req)
            );

        } catch (Exception e) {

            return ResponseEntity.status(409).body(
                    Map.of(
                            "message",
                            e.getMessage()
                    )
            );
        }
    }

    @GetMapping("/pricing")
    public ResponseEntity<List<CarPricing>> getAllPricing() {

        return ResponseEntity.ok(
                superAdminService.getAllPricing()
        );
    }

    @GetMapping("/pricing/{carClass}")
    public ResponseEntity<CarPricing> getPricingByClass(
            @PathVariable CarClass carClass
    ) {

        return ResponseEntity.ok(
                superAdminService.getPricingByClass(carClass)
        );
    }

    @PutMapping("/pricing/{id}")
    public ResponseEntity<CarPricing> updatePricing(
            @PathVariable Long id,
            @RequestBody CarPricingRequest req
    ) {

        return ResponseEntity.ok(
                superAdminService.updateCarPricing(id, req)
        );
    }

    @DeleteMapping("/pricing/{id}")
    public ResponseEntity<Map<String, String>> deletePricing(
            @PathVariable Long id
    ) {

        superAdminService.deleteCarPricing(id);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Pricing deleted successfully"
                )
        );
    }

    // ================= CARS =================

    @GetMapping("/cars")
    public ResponseEntity<List<Car>> getAllCars() {

        return ResponseEntity.ok(
                superAdminService.getAllCars()
        );
    }

    @GetMapping("/cars/status/{status}")
    public ResponseEntity<List<Car>> getCarsByStatus(
            @PathVariable CarStatus status
    ) {

        return ResponseEntity.ok(
                superAdminService.getCarsByStatus(status)
        );
    }

    @GetMapping("/cars/city/{city}")
    public ResponseEntity<List<Car>> getCarsByCity(
            @PathVariable String city
    ) {

        return ResponseEntity.ok(
                superAdminService.getCarsByCity(city)
        );
    }

    @GetMapping("/cars/city/{city}/available")
    public ResponseEntity<List<Car>> getAvailableCarsByCity(
            @PathVariable String city
    ) {

        return ResponseEntity.ok(
                superAdminService.getAvailableCarsByCity(city)
        );
    }

    @GetMapping("/cars/admin/{adminId}")
    public ResponseEntity<List<Car>> getCarsByAdmin(
            @PathVariable Long adminId
    ) {

        return ResponseEntity.ok(
                superAdminService.getCarsByAdmin(adminId)
        );
    }

    @GetMapping("/cars/repositioning")
    public ResponseEntity<List<Car>> getCarsNeedingRepositioning() {

        return ResponseEntity.ok(
                superAdminService.getCarsNeedingRepositioning()
        );
    }
    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStatsResponse>
    getDashboardStats() {

        return ResponseEntity.ok(
                superAdminService.getDashboardStats()
        );
    }
}