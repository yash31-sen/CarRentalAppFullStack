package carRental.CarRrental.Controllers;

import carRental.CarRrental.Dtos.*;
import carRental.CarRrental.Models.HostVerificationStatus;
import carRental.CarRrental.Models.InspectionStatus;
import carRental.CarRrental.Models.InspectionType;
import carRental.CarRrental.Services.AdminMarketplaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminPortalController {

    private final AdminMarketplaceService marketplaceService;

    // ================= OVERVIEW DASHBOARD =================

    @GetMapping("/dashboard/stats")
    public AdminDashboardResponseDto getDashboardStats(
            @RequestParam(required = false) String city,
            @AuthenticationPrincipal UserDetails userDetails) {
        return marketplaceService.getDashboardStats(userDetails.getUsername(), city);
    }

    // ================= HOST MANAGEMENT =================

    @GetMapping("/hosts")
    public List<HostSummaryDto> getHosts(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) HostVerificationStatus verificationStatus,
            @RequestParam(required = false) String accountStatus,
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal UserDetails userDetails) {
        return marketplaceService.getCityHosts(
                userDetails.getUsername(), city, verificationStatus, accountStatus, search);
    }

    @GetMapping("/hosts/{id}")
    public HostDetailDto getHostDetails(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return marketplaceService.getHostDetails(id, userDetails.getUsername());
    }

    @PatchMapping("/hosts/{id}/approve")
    public HostSummaryDto approveHost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return marketplaceService.verifyHost(
                id, userDetails.getUsername(), HostVerificationStatus.VERIFIED, null);
    }

    @PatchMapping("/hosts/{id}/reject")
    public HostSummaryDto rejectHost(
            @PathVariable Long id,
            @RequestBody AdminActionReasonRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        return marketplaceService.verifyHost(
                id, userDetails.getUsername(), HostVerificationStatus.REJECTED, req != null ? req.getReason() : null);
    }

    @PatchMapping("/hosts/{id}/request-resubmission")
    public HostSummaryDto requestHostResubmission(
            @PathVariable Long id,
            @RequestBody AdminActionReasonRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        return marketplaceService.verifyHost(
                id, userDetails.getUsername(), HostVerificationStatus.RESUBMISSION_REQUESTED, req != null ? req.getReason() : null);
    }

    @PatchMapping("/hosts/{id}/suspend")
    public ResponseEntity<Map<String, String>> suspendHost(
            @PathVariable Long id,
            @RequestBody AdminActionReasonRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        marketplaceService.suspendHost(id, userDetails.getUsername(), req != null ? req.getReason() : null);
        return ResponseEntity.ok(Map.of("message", "Host suspended successfully"));
    }

    @PatchMapping("/hosts/{id}/deactivate")
    public ResponseEntity<Map<String, String>> deactivateHost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        marketplaceService.setHostActiveStatus(id, userDetails.getUsername(), false);
        return ResponseEntity.ok(Map.of("message", "Host deactivated successfully"));
    }

    @PatchMapping("/hosts/{id}/reactivate")
    public ResponseEntity<Map<String, String>> reactivateHost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        marketplaceService.setHostActiveStatus(id, userDetails.getUsername(), true);
        return ResponseEntity.ok(Map.of("message", "Host reactivated successfully"));
    }

    // ================= BOOKING ISSUES & ESCALATIONS =================

    @GetMapping("/issues")
    public List<AdminIssueDto> getIssues(
            @RequestParam(required = false) String city,
            @AuthenticationPrincipal UserDetails userDetails) {
        return marketplaceService.getCityIssues(userDetails.getUsername(), city);
    }

    @GetMapping("/support-agents")
    public List<StaffSummaryDto> getSupportAgents() {
        return marketplaceService.getAvailableSupportAgents();
    }

    @PatchMapping("/issues/{id}/escalate")
    public AdminIssueDto escalateIssue(
            @PathVariable Long id,
            @RequestBody AdminActionReasonRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        return marketplaceService.escalateIssue(
                id,
                userDetails.getUsername(),
                req != null ? req.getSupportAgentId() : null,
                req != null ? req.getRemarks() : null);
    }

    @PatchMapping("/issues/{id}/resolve")
    public AdminIssueDto resolveIssue(
            @PathVariable Long id,
            @RequestBody AdminActionReasonRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        return marketplaceService.resolveIssue(
                id,
                userDetails.getUsername(),
                req != null ? req.getResolutionDetails() : null,
                req != null ? req.getStatus() : "RESOLVED");
    }

    // ================= INSPECTION MANAGEMENT =================

    @GetMapping("/inspections")
    public List<AdminInspectionDto> getInspections(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) InspectionStatus status,
            @RequestParam(required = false) InspectionType type,
            @AuthenticationPrincipal UserDetails userDetails) {
        return marketplaceService.getCityInspections(userDetails.getUsername(), city, status, type);
    }

    @GetMapping("/inspections/{id}")
    public AdminInspectionDto getInspectionDetails(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return marketplaceService.getInspectionDetails(id, userDetails.getUsername());
    }

    @PatchMapping("/inspections/{id}/escalate")
    public AdminInspectionDto escalateInspection(
            @PathVariable Long id,
            @RequestBody AdminActionReasonRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        boolean suspend = req != null && Boolean.TRUE.equals(req.getSuspendVehicle());
        return marketplaceService.escalateInspection(
                id, userDetails.getUsername(), req != null ? req.getRemarks() : null, suspend);
    }

    @PostMapping("/inspections/{id}/execute")
    @PreAuthorize("hasRole('FLEET_MANAGER')")
    public void executePhysicalInspectionBlocked(@PathVariable Long id) {
        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Physical inspections can only be executed by Fleet Managers."
        );
    }
}
