package carRental.CarRrental.Controllers;

import carRental.CarRrental.Dtos.*;
import carRental.CarRrental.Models.*;
import carRental.CarRrental.Services.SuperAdminMarketplaceService;
import carRental.CarRrental.Services.SuperAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    private final SuperAdminMarketplaceService marketplaceService;

    // ================= ADMINS =================

    @PostMapping("/admins")
    public ResponseEntity<Map<String, String>> createAdmin(
            @jakarta.validation.Valid @RequestBody CreateAdminRequest req
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
    public ResponseEntity<List<AdminSummaryDto>> getAllAdmins() {
        return ResponseEntity.ok(
                superAdminService.getAllAdminSummaries()
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
    public ResponseEntity<List<UserSummaryDto>> getAllUsers() {
        return ResponseEntity.ok(
                marketplaceService.getAllUserSummaries()
        );
    }

    @GetMapping("/users/{id}/details")
    public ResponseEntity<UserDetailDto> getUserDetails(@PathVariable Long id) {
        return ResponseEntity.ok(marketplaceService.getUserDetails(id));
    }

    @PatchMapping("/users/{id}/suspend")
    public ResponseEntity<Map<String, String>> suspendUser(
            @PathVariable Long id,
            @RequestBody(required = false) SuspendAccountRequest req) {
        marketplaceService.suspendUser(id, req != null ? req.getReason() : null);
        return ResponseEntity.ok(Map.of("message", "User suspended successfully"));
    }

    @PatchMapping("/users/{id}/unsuspend")
    public ResponseEntity<Map<String, String>> unsuspendUser(@PathVariable Long id) {
        marketplaceService.unsuspendUser(id);
        return ResponseEntity.ok(Map.of("message", "User unsuspended successfully"));
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

    @DeleteMapping("/users")
    public ResponseEntity<Map<String, String>> deleteAllUsers() {
        int count = superAdminService.deleteAllUsers();
        return ResponseEntity.ok(
                Map.of("message", "Deleted " + count + " user(s) with role USER")
        );
    }

    @DeleteMapping("/admins")
    public ResponseEntity<Map<String, String>> deleteAllAdmins() {
        int count = superAdminService.deleteAllAdmins();
        return ResponseEntity.ok(
                Map.of("message", "Deleted " + count + " user(s) with role ADMIN")
        );
    }

    @DeleteMapping("/drivers/all") // Used /drivers/all to not conflict if /drivers expects something else, or just /drivers
    public ResponseEntity<Map<String, String>> deleteAllDrivers() {
        int count = superAdminService.deleteAllDrivers();
        return ResponseEntity.ok(
                Map.of("message", "Deleted " + count + " user(s) with role DRIVER")
        );
    }

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
            @jakarta.validation.Valid @RequestBody ServiceCityRequest req
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
    public ResponseEntity<List<ServiceCityResponseDto>> getAllCities() {
        return ResponseEntity.ok(
                marketplaceService.getAllCitiesWithMetrics()
        );
    }

    @PutMapping("/cities/{id}")
    public ResponseEntity<ServiceCity> updateCity(
            @PathVariable Long id,
            @Valid @RequestBody ServiceCityRequest req) {
        return ResponseEntity.ok(marketplaceService.updateCity(id, req));
    }

    @DeleteMapping("/cities/{id}")
    public ResponseEntity<Map<String, String>> deleteCity(@PathVariable Long id) {
        marketplaceService.deleteCity(id);
        return ResponseEntity.ok(Map.of("message", "City deleted successfully"));
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
            @jakarta.validation.Valid @RequestBody CarPricingRequest req
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
            @jakarta.validation.Valid @RequestBody CarPricingRequest req
    ) {

        return ResponseEntity.ok(
                superAdminService.updateCarPricing(id, req)
        );
    }

    @PatchMapping("/pricing/{id}/deactivate")
    public ResponseEntity<CarPricing> deactivatePricing(@PathVariable Long id) {
        return ResponseEntity.ok(superAdminService.setPricingActiveStatus(id, false));
    }

    @PatchMapping("/pricing/{id}/reactivate")
    public ResponseEntity<CarPricing> reactivatePricing(@PathVariable Long id) {
        return ResponseEntity.ok(superAdminService.setPricingActiveStatus(id, true));
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
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        return ResponseEntity.ok(superAdminService.getDashboardStats());
    }

    // ================= HOSTS (REPLACES DRIVERS) =================

    @GetMapping("/hosts")
    public ResponseEntity<List<HostSummaryDto>> getAllHosts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) HostVerificationStatus verificationStatus,
            @RequestParam(required = false) Boolean active) {
        return ResponseEntity.ok(marketplaceService.getAllHosts(search, verificationStatus, active));
    }

    @GetMapping("/hosts/{id}")
    public ResponseEntity<HostDetailDto> getHostDetails(@PathVariable Long id) {
        return ResponseEntity.ok(marketplaceService.getHostDetails(id));
    }

    @PatchMapping("/hosts/{id}/verify")
    public ResponseEntity<HostSummaryDto> verifyHost(
            @PathVariable Long id,
            @Valid @RequestBody HostVerificationRequest req) {
        return ResponseEntity.ok(marketplaceService.verifyHost(id, req));
    }

    @PatchMapping("/hosts/{id}/deactivate")
    public ResponseEntity<Map<String, String>> deactivateHost(@PathVariable Long id) {
        marketplaceService.setHostActiveStatus(id, false);
        return ResponseEntity.ok(Map.of("message", "Host deactivated successfully"));
    }

    @PatchMapping("/hosts/{id}/reactivate")
    public ResponseEntity<Map<String, String>> reactivateHost(@PathVariable Long id) {
        marketplaceService.setHostActiveStatus(id, true);
        return ResponseEntity.ok(Map.of("message", "Host reactivated successfully"));
    }

    @PatchMapping("/hosts/{id}/suspend")
    public ResponseEntity<Map<String, String>> suspendHost(
            @PathVariable Long id,
            @RequestBody(required = false) SuspendAccountRequest req) {
        marketplaceService.suspendHost(id, req != null ? req.getReason() : null);
        return ResponseEntity.ok(Map.of("message", "Host suspended successfully"));
    }

    @PatchMapping("/hosts/{id}/unsuspend")
    public ResponseEntity<Map<String, String>> unsuspendHost(@PathVariable Long id) {
        marketplaceService.unsuspendHost(id);
        return ResponseEntity.ok(Map.of("message", "Host unsuspended successfully"));
    }

    // ================= FLEET MANAGERS =================

    @GetMapping("/fleet-managers")
    public ResponseEntity<List<StaffSummaryDto>> getFleetManagers() {
        return ResponseEntity.ok(marketplaceService.getStaffByRole(UserRole.FLEET_MANAGER));
    }

    @PostMapping("/fleet-managers")
    public ResponseEntity<StaffSummaryDto> createFleetManager(@Valid @RequestBody StaffAccountRequest req) {
        return ResponseEntity.status(201).body(marketplaceService.createStaffAccount(req, UserRole.FLEET_MANAGER));
    }

    @PatchMapping("/fleet-managers/{id}/assign-scope")
    public ResponseEntity<StaffSummaryDto> assignFleetManagerScope(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(marketplaceService.assignStaffScope(
                id, UserRole.FLEET_MANAGER, body.get("city"), body.get("operationalScope")));
    }

    @PatchMapping("/fleet-managers/{id}/deactivate")
    public ResponseEntity<Map<String, String>> deactivateFleetManager(@PathVariable Long id) {
        marketplaceService.setStaffActiveStatus(id, UserRole.FLEET_MANAGER, false);
        return ResponseEntity.ok(Map.of("message", "Fleet Manager deactivated successfully"));
    }

    @PatchMapping("/fleet-managers/{id}/reactivate")
    public ResponseEntity<Map<String, String>> reactivateFleetManager(@PathVariable Long id) {
        marketplaceService.setStaffActiveStatus(id, UserRole.FLEET_MANAGER, true);
        return ResponseEntity.ok(Map.of("message", "Fleet Manager reactivated successfully"));
    }

    // ================= SUPPORT AGENTS =================

    @GetMapping("/support-agents")
    public ResponseEntity<List<StaffSummaryDto>> getSupportAgents() {
        return ResponseEntity.ok(marketplaceService.getStaffByRole(UserRole.SUPPORT_AGENT));
    }

    @PostMapping("/support-agents")
    public ResponseEntity<StaffSummaryDto> createSupportAgent(@Valid @RequestBody StaffAccountRequest req) {
        return ResponseEntity.status(201).body(marketplaceService.createStaffAccount(req, UserRole.SUPPORT_AGENT));
    }

    @PatchMapping("/support-agents/{id}/assign-scope")
    public ResponseEntity<StaffSummaryDto> assignSupportAgentScope(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(marketplaceService.assignStaffScope(
                id, UserRole.SUPPORT_AGENT, body.get("city"), body.get("operationalScope")));
    }

    @PatchMapping("/support-agents/{id}/deactivate")
    public ResponseEntity<Map<String, String>> deactivateSupportAgent(@PathVariable Long id) {
        marketplaceService.setStaffActiveStatus(id, UserRole.SUPPORT_AGENT, false);
        return ResponseEntity.ok(Map.of("message", "Support Agent deactivated successfully"));
    }

    @PatchMapping("/support-agents/{id}/reactivate")
    public ResponseEntity<Map<String, String>> reactivateSupportAgent(@PathVariable Long id) {
        marketplaceService.setStaffActiveStatus(id, UserRole.SUPPORT_AGENT, true);
        return ResponseEntity.ok(Map.of("message", "Support Agent reactivated successfully"));
    }

    // ================= PLATFORM SETTINGS & AUDIT LOGS =================

    @GetMapping("/settings")
    public ResponseEntity<PlatformSettingsDto> getPlatformSettings() {
        return ResponseEntity.ok(marketplaceService.getPlatformSettings());
    }

    @PutMapping("/settings")
    public ResponseEntity<PlatformSettingsDto> updatePlatformSettings(@Valid @RequestBody PlatformSettingsDto dto) {
        return ResponseEntity.ok(marketplaceService.updatePlatformSettings(dto));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<Page<AuditLog>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(marketplaceService.getAuditLogs(PageRequest.of(page, size)));
    }
}