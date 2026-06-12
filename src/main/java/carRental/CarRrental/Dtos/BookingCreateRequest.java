package carRental.CarRrental.Dtos;

import carRental.CarRrental.Models.TripType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BookingCreateRequest {

    private Long carId;

    private RegisterRequest.BookingType bookingType;        // SELF_DRIVE or WITH_DRIVER

    private TripType tripType;              // 👈 NEW - ROUND_TRIP or ONE_WAY

    private LocalDate startDate;
    private LocalDate endDate;

    private String pickupCity;              // 👈 NEW - "Indore"
    private String dropCity;               // 👈 NEW - "Bhopal" or "Indore"

    private String pickupLocation;          // "Indore Railway Station"
    private String dropLocation;            // "Bhopal Bus Stand"

    // required only for self drive
    private String drivingLicenseNumber;
}