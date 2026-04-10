package carRental.CarRrental.Dtos;

import carRental.CarRrental.Models.CarClass;
import carRental.CarRrental.Models.FuelType;
import carRental.CarRrental.Models.TransmissionType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CarCreateRequest {
    private String carNumber;
    private String brand;
    private String model;
    private CarClass carClass;
    private FuelType fuelType;
    private TransmissionType transmission;
    private double pricePerDay;
    private String currentLocation;
}
