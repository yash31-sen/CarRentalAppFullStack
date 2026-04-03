package carRental.CarRrental.Dtos;

import carRental.CarRrental.Models.BookingType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BookingCreateRequest {

    private Long carId;

    private BookingType bookingType; // SELF_DRIVE or WITH_DRIVER

    private LocalDate startDate;
    private LocalDate endDate;

    private String pickupLocation;
    private String dropLocation;

    // required only for self drive
    private String drivingLicenseNumber;
}
