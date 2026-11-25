package carRental.CarRrental;

import carRental.CarRrental.Dtos.RegisterRequest;
import carRental.CarRrental.Models.*;
import carRental.CarRrental.Repositories.*;
import carRental.CarRrental.Services.AuthService;
import carRental.CarRrental.Services.JwtService;
import carRental.CarRrental.Services.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class HostAuthOnboardingSecurityTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private AuthService authService;
    @Autowired private TokenService tokenService;
    @Autowired private JwtService jwtService;
    @Autowired private AppUserRepository userRepository;
    @Autowired private UserTokenRepository userTokenRepository;
    @Autowired private HostProfileRepository hostProfileRepository;
    @Autowired private HostStatusHistoryRepository hostStatusHistoryRepository;
    @Autowired private ServiceCityRepository serviceCityRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void publicHostRegistrationCreatesUserOnlyAndBlocksDuplicateHostSignup() {
        RegisterRequest req = new RegisterRequest();
        req.setName("Alice Host");
        req.setEmail("alice.host@example.com");
        req.setPassword("Pass@1234");
        req.setMobileNumber("9876543210");
        req.setRegistrationIntent("HOST_APPLICATION");
        req.setTermsAccepted(true);

        authService.register(req);

        AppUser saved = userRepository.findByEmail("alice.host@example.com").orElseThrow();
        assertEquals(UserRole.USER, saved.getRole());
        assertFalse(saved.hasRole(UserRole.HOST));
        assertFalse(saved.isEmailVerified());
        assertFalse(Boolean.TRUE.equals(saved.getMobileVerified()));

        HostProfile profile = hostProfileRepository.findByUser_Id(saved.getId()).orElseThrow();
        assertEquals(HostVerificationStatus.PENDING_EMAIL_VERIFICATION, profile.getVerificationStatus());

        // Duplicate HOST_APPLICATION on same email must fail with 409 Conflict
        RegisterRequest dup = new RegisterRequest();
        dup.setName("Alice Clone");
        dup.setEmail("alice.host@example.com");
        dup.setPassword("Pass@1234");
        dup.setRegistrationIntent("HOST_APPLICATION");
        dup.setTermsAccepted(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.register(dup));
        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    void emailTokenIsHashedSingleUseRevocableAndPurposeScoped() {
        AppUser user = userRepository.save(AppUser.builder()
                .name("Bob User")
                .email("bob.token@example.com")
                .passwordHash(passwordEncoder.encode("Pass@1234"))
                .role(UserRole.USER)
                .emailVerified(false)
                .isActive(true)
                .build());

        TokenService.GeneratedToken first = tokenService.createEmailVerificationToken(user);
        assertNotEquals(first.rawToken(), first.entity().getToken());
        assertEquals(64, first.entity().getToken().length()); // SHA-256 hex

        // Reissuing revokes the first token
        TokenService.GeneratedToken second = tokenService.createEmailVerificationToken(user);
        UserToken reloadedFirst = userTokenRepository.findById(first.entity().getId()).orElseThrow();
        assertTrue(reloadedFirst.isRevoked());

        // Revoked first token cannot verify
        assertThrows(ResponseStatusException.class, () -> authService.verifyEmail(first.rawToken()));

        // Password reset token cannot be used on email verification
        TokenService.GeneratedToken reset = tokenService.createPasswordResetToken(user);
        assertThrows(ResponseStatusException.class, () -> authService.verifyEmail(reset.rawToken()));

        // Valid second token verifies email once and rejects reuse
        var authRes = authService.verifyEmail(second.rawToken());
        assertTrue(authRes.isEmailVerified());
        assertThrows(ResponseStatusException.class, () -> authService.verifyEmail(second.rawToken()));
    }

    @Test
    void expiredTokenIsRejected() {
        AppUser user = userRepository.save(AppUser.builder()
                .name("Expired User")
                .email("expired.token@example.com")
                .passwordHash(passwordEncoder.encode("Pass@1234"))
                .role(UserRole.USER)
                .emailVerified(false)
                .isActive(true)
                .build());

        TokenService.GeneratedToken token = tokenService.createEmailVerificationToken(user);
        token.entity().setExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS));
        userTokenRepository.save(token.entity());

        assertThrows(ResponseStatusException.class, () -> authService.verifyEmail(token.rawToken()));
    }

    @Test
    void unconfiguredSmsProviderLeavesMobilePending() {
        AppUser user = userRepository.save(AppUser.builder()
                .name("Sms User")
                .email("sms.user@example.com")
                .passwordHash(passwordEncoder.encode("Pass@1234"))
                .role(UserRole.USER)
                .emailVerified(true)
                .isActive(true)
                .build());

        var res = authService.sendMobileVerification("sms.user@example.com", "9998887776");
        assertEquals(false, res.get("smsConfigured"));
        assertEquals(false, res.get("mobileVerified"));
    }

    @Test
    void adminApprovalGrantsHostRolePreservesUserAndBumpsTokenVersion() throws Exception {
        if (!serviceCityRepository.existsByCityNameIgnoreCaseAndActiveTrue("Indore")) {
            serviceCityRepository.save(ServiceCity.builder().cityName("Indore").active(true).build());
        }

        userRepository.save(AppUser.builder()
                .name("City Admin")
                .email("city.admin@example.com")
                .passwordHash("encoded")
                .role(UserRole.ADMIN)
                .city("Indore")
                .emailVerified(true)
                .isActive(true)
                .build());

        AppUser applicant = userRepository.save(AppUser.builder()
                .name("Dual Role User")
                .email("dual.host@example.com")
                .passwordHash("encoded")
                .role(UserRole.USER)
                .emailVerified(true)
                .isActive(true)
                .tokenVersion(0)
                .build());

        String staleJwt = jwtService.generateToken(applicant);

        HostProfile profile = hostProfileRepository.save(HostProfile.builder()
                .user(applicant)
                .city("Indore")
                .phone("9876543210")
                .verificationStatus(HostVerificationStatus.PENDING_ADMIN_APPROVAL)
                .active(false)
                .build());

        mockMvc.perform(patch("/admin/hosts/" + profile.getId() + "/approve")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user("city.admin@example.com").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationStatus").value("VERIFIED"));

        AppUser updated = userRepository.findById(applicant.getId()).orElseThrow();
        assertTrue(updated.hasRole(UserRole.USER));
        assertTrue(updated.hasRole(UserRole.HOST));
        assertEquals(1, updated.getTokenVersion());
        assertFalse(hostStatusHistoryRepository.findByHostProfile_IdOrderByCreatedAtDesc(profile.getId()).isEmpty());

        // Stale JWT with old tokenVersion=0 must be rejected
        mockMvc.perform(get("/host/dashboard")
                        .header("Authorization", "Bearer " + staleJwt))
                .andExpect(status().isUnauthorized());

        // Fresh JWT with updated tokenVersion=1 and HOST role must be accepted
        String freshJwt = jwtService.generateToken(updated);
        mockMvc.perform(get("/host/dashboard")
                        .header("Authorization", "Bearer " + freshJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("Indore"));
    }

    @Test
    @WithMockUser(username = "plain.user@example.com", roles = {"USER"})
    void userWithoutHostRoleCannotAccessHostPortalOrOtherUsersDocuments() throws Exception {
        mockMvc.perform(get("/host/dashboard"))
                .andExpect(status().isForbidden());

        userRepository.save(AppUser.builder()
                .name("Plain User")
                .email("plain.user@example.com")
                .passwordHash("encoded")
                .role(UserRole.USER)
                .emailVerified(true)
                .isActive(true)
                .build());

        mockMvc.perform(get("/api/documents/host/999999/fake-doc.pdf"))
                .andExpect(status().isForbidden());
    }
}
