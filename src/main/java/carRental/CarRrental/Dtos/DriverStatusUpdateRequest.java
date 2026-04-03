package carRental.CarRrental.Dtos;

import carRental.CarRrental.Models.DriverStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DriverStatusUpdateRequest {
    private DriverStatus status; // AVAILABLE, ON_TRIP, INACTIVE
}
