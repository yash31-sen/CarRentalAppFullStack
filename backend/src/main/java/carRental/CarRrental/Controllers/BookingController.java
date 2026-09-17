package carRental.CarRrental.Controllers;

import carRental.CarRrental.Dtos.BookingCreateRequest;
import carRental.CarRrental.Models.Booking;
import carRental.CarRrental.Services.BookingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import carRental.CarRrental.Dtos.PaymentSuccessRequest;
import java.util.List;
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public Booking create(Authentication authentication,
                          @jakarta.validation.Valid @RequestBody BookingCreateRequest req) {

        String email = authentication.getName();
        return bookingService.createBooking(email, req);
    }


    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{id}/payment-success")
    public Booking paymentSuccess(Authentication authentication,
                                  @PathVariable Long id,
                                  @RequestBody PaymentSuccessRequest req) {
        String email = authentication.getName();
        return bookingService.markPaymentSuccess(id, email, req.getPaymentRef());
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/my")
    public List<Booking> getMyBookings(Authentication authentication) {
        String email = authentication.getName();
        return bookingService.getBookingsForUser(email);
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{id}/cancel")
    public Booking cancelBooking(Authentication authentication, @PathVariable Long id) {
        String email = authentication.getName();
        return bookingService.cancelBooking(id, email);
    }

}
