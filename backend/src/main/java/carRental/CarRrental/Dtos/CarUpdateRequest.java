package carRental.CarRrental.Dtos;

import carRental.CarRrental.Models.CarClass;
import carRental.CarRrental.Models.CarStatus;
import carRental.CarRrental.Models.FuelType;
import carRental.CarRrental.Models.TransmissionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CarUpdateRequest {

    @NotBlank(message = "Brand is required")
    private String brand;

    @NotBlank(message = "Model is required")
    private String model;

    @NotNull(message = "Car class is required")
    private CarClass carClass;

    @NotNull(message = "Fuel type is required")
    private FuelType fuelType;

    @NotNull(message = "Transmission is required")
    private TransmissionType transmission;

    @NotNull(message = "Price per day is required")
    @Min(value = 1, message = "Price per day must be at least 1")
    private Double pricePerDay;

    @NotBlank(message = "Current location is required")
    private String currentLocation;

    @NotNull(message = "Status is required")
    private CarStatus status;

    @NotNull(message = "Active is required")
    private Boolean active;
}