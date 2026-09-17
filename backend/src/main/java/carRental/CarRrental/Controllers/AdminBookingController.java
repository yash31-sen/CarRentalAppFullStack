package carRental.CarRrental.Controllers;

import carRental.CarRrental.Models.Booking;
import carRental.CarRrental.Services.BookingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/bookings")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminBookingController {

    private final BookingService bookingService;

    public AdminBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public List<Booking> getAllBookings(@AuthenticationPrincipal UserDetails userDetails) {
        return bookingService.getBookingsForAdmin(userDetails.getUsername());
    }

    @PostMapping("/{id}/verify-license")
    public Booking verifyLicense(
            @PathVariable Long id,
            @RequestParam boolean approved,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return bookingService.verifyLicense(id, approved, userDetails.getUsername());
    }

    @PostMapping("/{id}/assign-driver/{driverId}")
    public Booking assignDriver(
            @PathVariable Long id,
            @PathVariable Long driverId
    ) {
        return bookingService.assignDriver(id, driverId);
    }

    @PostMapping("/{id}/complete-inspection")
    public Booking completeInspection(@PathVariable Long id) {
        return bookingService.completeBooking(id);
    }
}
