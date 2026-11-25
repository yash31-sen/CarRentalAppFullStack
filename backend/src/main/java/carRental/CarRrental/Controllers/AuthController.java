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

import java.util.HashMap;
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
    public ResponseEntity<Map<String, String>> register(@jakarta.validation.Valid @RequestBody RegisterRequest request) {
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
    public AuthResponse login(@jakarta.validation.Valid @RequestBody LoginRequest request) {
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
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        Map<String, String> response = new HashMap<>();
        response.put("message", "If the email exists, a reset link has been sent.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password reset successful.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/setup-account")
    public ResponseEntity<Map<String, String>> setupAccount(@RequestBody ResetPasswordRequest request) {
        authService.setupAccount(request);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Account setup successful.");
        return ResponseEntity.ok(response);
    }

}
