package carRental.CarRrental.Dtos;

import carRental.CarRrental.Models.TripType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BookingCreateRequest {

    @NotNull(message = "Car ID is required")
    private Long carId;

    @NotNull(message = "Booking type is required")
    private RegisterRequest.BookingType bookingType;        // SELF_DRIVE or WITH_DRIVER

    @NotNull(message = "Trip type is required")
    private TripType tripType;              // ROUND_TRIP or ONE_WAY

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotBlank(message = "Pickup city is required")
    private String pickupCity;

    @NotBlank(message = "Drop city is required")
    private String dropCity;

    @NotBlank(message = "Pickup location is required")
    private String pickupLocation;

    @NotBlank(message = "Drop location is required")
    private String dropLocation;

    // required only for self drive
    private String drivingLicenseNumber;
}