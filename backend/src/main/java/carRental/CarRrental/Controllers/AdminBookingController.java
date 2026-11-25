package carRental.CarRrental.Controllers;

import carRental.CarRrental.Dtos.AdminActionReasonRequest;
import carRental.CarRrental.Dtos.AdminBookingDetailDto;
import carRental.CarRrental.Dtos.AdminBookingDto;
import carRental.CarRrental.Models.Booking;
import carRental.CarRrental.Models.BookingStatus;
import carRental.CarRrental.Services.AdminMarketplaceService;
import carRental.CarRrental.Services.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/admin/bookings")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminBookingController {

    private final AdminMarketplaceService marketplaceService;
    private final BookingService bookingService;

    @GetMapping
    public List<AdminBookingDto> getAllBookings(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal UserDetails userDetails) {
        return marketplaceService.getCitySelfDriveBookings(
                userDetails.getUsername(), city, status, paymentStatus, search);
    }

    @GetMapping("/{id}")
    public AdminBookingDetailDto getBookingDetails(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return marketplaceService.getBookingDetails(id, userDetails.getUsername());
    }

    @PostMapping("/{id}/verify-license")
    public Booking verifyLicense(
            @PathVariable Long id,
            @RequestParam boolean approved,
            @RequestParam(required = false, defaultValue = "false") boolean resubmissionRequested,
            @RequestParam(required = false) String remarks,
            @RequestBody(required = false) AdminActionReasonRequest body,
            @AuthenticationPrincipal UserDetails userDetails) {
        String effectiveRemarks = (body != null && body.getRemarks() != null) ? body.getRemarks() : remarks;
        return marketplaceService.verifyLicense(
                id, approved, resubmissionRequested, effectiveRemarks, userDetails.getUsername());
    }

    @PostMapping("/{id}/assign-driver/{driverId}")
    public Booking assignDriverBlocked(@PathVariable Long id, @PathVariable Long driverId) {
        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Driver assignment has been removed. This marketplace supports self-drive bookings only."
        );
    }

    @PostMapping("/{id}/complete-inspection")
    public Booking completeInspection(@PathVariable Long id) {
        return bookingService.completeBooking(id);
    }
}
