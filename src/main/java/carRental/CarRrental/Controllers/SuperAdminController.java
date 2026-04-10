package carRental.CarRrental.Controllers;

import carRental.CarRrental.Dtos.CreateAdminRequest;
import carRental.CarRrental.Services.AuthService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/super-admin")
public class SuperAdminController {

    private final AuthService authService;

    public SuperAdminController(AuthService authService) {
        this.authService = authService;
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/create-admin")
    public String createAdmin(@RequestBody CreateAdminRequest req) {
        authService.createAdmin(req);
        return "Admin created successfully";
    }
}