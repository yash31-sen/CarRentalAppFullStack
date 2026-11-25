package carRental.CarRrental.Controllers;

import carRental.CarRrental.Dtos.*;
import carRental.CarRrental.Services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok(
                Map.of("message", "Registration successful. Please check your email for the verification link.")
        );
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, String>> resendVerification(@RequestBody Map<String, String> payload) {
        authService.resendVerificationEmail(payload.get("email"));
        return ResponseEntity.ok(
                Map.of("message", "If an unverified account exists for that email, a new verification link has been sent.")
        );
    }

    @GetMapping("/verify")
    public ResponseEntity<AuthResponse> verify(@RequestParam String token) {
        AuthResponse response = authService.verifyEmail(token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
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

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(Authentication authentication) {
        if (authentication != null) {
            authService.logout(authentication.getName());
        }
        return ResponseEntity.ok(Map.of("message", "Logged out successfully."));
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

    @PostMapping("/mobile/send-verification")
    public ResponseEntity<Map<String, Object>> sendMobileVerification(
            Authentication authentication,
            @RequestBody Map<String, String> payload
    ) {
        String mobileNumber = payload.get("mobileNumber");
        return ResponseEntity.ok(authService.sendMobileVerification(authentication.getName(), mobileNumber));
    }

    @GetMapping("/mobile/verify")
    public ResponseEntity<Map<String, Object>> verifyMobile(@RequestParam String token) {
        return ResponseEntity.ok(authService.verifyMobile(token));
    }
}
