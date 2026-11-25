package carRental.CarRrental.Controllers;

import carRental.CarRrental.Services.HostPortalService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/host")
public class HostPortalController {

    private final HostPortalService hostPortalService;

    public HostPortalController(HostPortalService hostPortalService) {
        this.hostPortalService = hostPortalService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard(Authentication authentication) {
        return ResponseEntity.ok(hostPortalService.getDashboard(authentication.getName()));
    }

    @GetMapping("/vehicles")
    public ResponseEntity<List<Map<String, Object>>> getVehicles(Authentication authentication) {
        return ResponseEntity.ok(hostPortalService.getMyVehicles(authentication.getName()));
    }

    @PostMapping("/vehicles")
    public ResponseEntity<Map<String, Object>> createVehicle(
            Authentication authentication,
            @RequestBody Map<String, Object> payload
    ) {
        return ResponseEntity.ok(hostPortalService.createVehicle(authentication.getName(), payload));
    }

    @PatchMapping("/vehicles/{carId}/availability")
    public ResponseEntity<Map<String, Object>> toggleAvailability(
            Authentication authentication,
            @PathVariable Long carId,
            @RequestBody Map<String, Boolean> payload
    ) {
        boolean active = Boolean.TRUE.equals(payload.get("active"));
        return ResponseEntity.ok(hostPortalService.toggleVehicleAvailability(authentication.getName(), carId, active));
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<Map<String, Object>>> getBookings(Authentication authentication) {
        return ResponseEntity.ok(hostPortalService.getMyBookings(authentication.getName()));
    }

    @GetMapping("/earnings")
    public ResponseEntity<Map<String, Object>> getEarnings(Authentication authentication) {
        return ResponseEntity.ok(hostPortalService.getEarningsSummary(authentication.getName()));
    }
}
