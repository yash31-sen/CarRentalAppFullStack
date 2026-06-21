package carRental.CarRrental.Dtos;

import carRental.CarRrental.Models.CarClass;
import carRental.CarRrental.Models.FuelType;
import carRental.CarRrental.Models.TransmissionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CarCreateRequest {

    @NotBlank(message = "Car number is required")
    @Pattern(regexp = "^[A-Z]{2}[0-9]{2}[A-Z]{1,2}[0-9]{4}$", message = "Car number must be in format MP09AB1234")
    private String carNumber;

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
    private double pricePerDay;
}