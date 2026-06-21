package carRental.CarRrental.Dtos;

import carRental.CarRrental.Models.CarClass;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CarPricingRequest {

    @NotNull(message = "Car class is required")
    private CarClass carClass;          // which class this rule is for

    @Min(value = 0, message = "Base fare cannot be negative")
    private double baseFare;            // flat booking fee

    @Min(value = 1, message = "Per day rate must be at least 1")
    private double perDayRate;          // default daily rate

    @Min(value = 1, message = "Minimum per day rate must be at least 1")
    private double minPerDayRate;       // minimum daily rate

    @Min(value = 1, message = "Maximum per day rate must be at least 1")
    private double maxPerDayRate;       // maximum daily rate

    @Min(value = 1, message = "Free KM per day must be at least 1")
    private double freeKmPerDay;        // free km per day

    @Min(value = 0, message = "Extra KM rate cannot be negative")
    private double extraKmRate;         // rate per extra km
}