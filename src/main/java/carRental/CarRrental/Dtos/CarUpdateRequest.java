package carRental.CarRrental.Dtos;

import carRental.CarRrental.Models.CarClass;
import carRental.CarRrental.Models.CarStatus;
import carRental.CarRrental.Models.FuelType;
import carRental.CarRrental.Models.TransmissionType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CarUpdateRequest {
    private String brand;
    private String model;
    private CarClass carClass;
    private FuelType fuelType;
    private TransmissionType transmission;
    private Double pricePerDay;
    private String currentLocation;     // admin can manually update
    // parking address if needed
    private CarStatus status;           // 👈 NEW - admin can update
    // AVAILABLE, MAINTENANCE etc
    private Boolean active;
}