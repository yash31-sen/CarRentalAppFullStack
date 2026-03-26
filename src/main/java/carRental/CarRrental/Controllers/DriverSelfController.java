package carRental.CarRrental.Controllers;

import carRental.CarRrental.Dtos.DriverLocationUpdateRequest;
import carRental.CarRrental.Dtos.DriverStatusUpdateRequest;
import carRental.CarRrental.Models.DriverProfile;
import carRental.CarRrental.Services.DriverSelfService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/driver")
public class DriverSelfController {

    private final DriverSelfService driverSelfService;

    public DriverSelfController(DriverSelfService driverSelfService) {
        this.driverSelfService = driverSelfService;
    }

    @PreAuthorize("hasRole('DRIVER')")
    @GetMapping("/me")
    public DriverProfile me(Authentication authentication) {
        String email = authentication.getName();
        return driverSelfService.getMyProfile(email);
    }

    @PreAuthorize("hasRole('DRIVER')")
    @PatchMapping("/location")
    public DriverProfile updateLocation(Authentication authentication,
                                        @RequestBody DriverLocationUpdateRequest req) {
        String email = authentication.getName();
        return driverSelfService.updateMyLocation(email, req.getCurrentLocation());
    }

    @PreAuthorize("hasRole('DRIVER')")
    @PatchMapping("/status")
    public DriverProfile updateStatus(Authentication authentication,
                                      @RequestBody DriverStatusUpdateRequest req) {
        String email = authentication.getName();
        return driverSelfService.updateMyStatus(email, req.getStatus());
    }
}
