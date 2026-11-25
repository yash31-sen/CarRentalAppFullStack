package carRental.CarRrental.Controllers;

import carRental.CarRrental.Dtos.AdminActionReasonRequest;
import carRental.CarRrental.Dtos.AdminCarDetailDto;
import carRental.CarRrental.Dtos.AdminCarListingDto;
import carRental.CarRrental.Models.CarApprovalStatus;
import carRental.CarRrental.Models.CarClass;
import carRental.CarRrental.Models.CarStatus;
import carRental.CarRrental.Services.AdminMarketplaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/admin/cars")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminCarController {

    private final AdminMarketplaceService marketplaceService;

    @PostMapping
    public void addCarBlocked() {
        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Admins cannot register vehicles. Hosts must submit vehicle listings through the Host Portal."
        );
    }

    @PutMapping("/{id}")
    public void updateCarBlocked(@PathVariable Long id) {
        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Admins cannot modify Host-owned vehicle details or rental prices."
        );
    }

    @GetMapping
    public List<AdminCarListingDto> getAll(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) CarApprovalStatus approvalStatus,
            @RequestParam(required = false) CarStatus status,
            @RequestParam(required = false) CarClass category,
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal UserDetails userDetails) {
        return marketplaceService.getCityVehicles(
                userDetails.getUsername(), city, approvalStatus, status, category, search);
    }

    @GetMapping("/{id}")
    public AdminCarDetailDto getById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return marketplaceService.getVehicleDetails(id, userDetails.getUsername());
    }

    @PatchMapping("/{id}/approve")
    public AdminCarListingDto approve(
            @PathVariable Long id,
            @RequestBody(required = false) AdminActionReasonRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        return marketplaceService.approveVehicle(
                id, userDetails.getUsername(), req != null ? req.getRemarks() : null);
    }

    @PatchMapping("/{id}/reject")
    public AdminCarListingDto reject(
            @PathVariable Long id,
            @RequestBody AdminActionReasonRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        return marketplaceService.rejectVehicle(
                id, userDetails.getUsername(), req != null ? req.getReason() : null);
    }

    @PatchMapping("/{id}/request-resubmission")
    public AdminCarListingDto requestResubmission(
            @PathVariable Long id,
            @RequestBody AdminActionReasonRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        return marketplaceService.requestVehicleResubmission(
                id, userDetails.getUsername(), req != null ? req.getReason() : null);
    }

    @PatchMapping("/{id}/suspend")
    public AdminCarListingDto suspend(
            @PathVariable Long id,
            @RequestBody AdminActionReasonRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        return marketplaceService.suspendVehicle(
                id, userDetails.getUsername(), req != null ? req.getReason() : null);
    }

    @PatchMapping("/{id}/disable")
    @DeleteMapping("/{id}")
    public AdminCarListingDto disable(
            @PathVariable Long id,
            @RequestBody(required = false) AdminActionReasonRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        return marketplaceService.disableVehicle(
                id, userDetails.getUsername(), req != null ? req.getReason() : null);
    }

    @PatchMapping("/{id}/reactivate")
    public AdminCarListingDto reactivate(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return marketplaceService.reactivateVehicle(id, userDetails.getUsername());
    }
}
