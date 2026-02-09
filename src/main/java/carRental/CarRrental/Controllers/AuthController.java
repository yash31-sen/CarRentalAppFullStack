package carRental.CarRrental.Controllers;

import carRental.CarRrental.Dtos.RegisterRequest;
import carRental.CarRrental.Services.AuthService;
import org.springframework.web.bind.annotation.*;
import carRental.CarRrental.Dtos.AuthResponse;
import carRental.CarRrental.Dtos.LoginRequest;
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // Register API
    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return "Registration successful. Please check email for verification link.";
    }

    // Email Verify API
    @GetMapping("/verify")
    public String verify(@RequestParam String token) {
        authService.verifyEmail(token);
        return "Email verified successfully.";
    }
    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
