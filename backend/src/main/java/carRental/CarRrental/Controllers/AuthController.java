package carRental.CarRrental.Controllers;

import carRental.CarRrental.Dtos.RegisterRequest;
import carRental.CarRrental.Services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import carRental.CarRrental.Dtos.AuthResponse;
import carRental.CarRrental.Dtos.LoginRequest;
import carRental.CarRrental.Dtos.ForgotPasswordRequest;
import carRental.CarRrental.Dtos.ResetPasswordRequest;
import carRental.CarRrental.Dtos.GoogleLoginRequest;
import carRental.CarRrental.Dtos.RefreshTokenRequest;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // Register API
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody RegisterRequest request) {
        authService.register(request);

        return ResponseEntity.ok(
                Map.of("message", "Registration successful. Please check email for verification link.")
        );
    }

    // Email Verify API
    @GetMapping("/verify")
    public ResponseEntity<AuthResponse> verify(
            @RequestParam String token) {
        AuthResponse response = authService.verifyEmail(token);
        return ResponseEntity.ok(response);     // ✅ returns JWT!
    }
    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
    @PostMapping("/google")
    public AuthResponse googleLogin(@RequestBody GoogleLoginRequest request) {
        return authService.googleLogin(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestBody RefreshTokenRequest request) {
        return authService.refreshToken(request);
    }



    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return "If the email exists, a reset link has been sent.";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return "Password reset successful.";
    }

}
