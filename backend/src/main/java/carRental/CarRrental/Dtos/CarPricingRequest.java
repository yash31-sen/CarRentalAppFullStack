package carRental.CarRrental.Dtos;

import carRental.CarRrental.Models.CarClass;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CarPricingRequest {

    @NotNull(message = "Car class is required")
    private CarClass carClass;

    @Min(value = 0, message = "Base fare cannot be negative")
    private double baseFare;

    @Min(value = 1, message = "Per day rate must be at least 1")
    private double perDayRate;

    @Min(value = 1, message = "Minimum per day rate must be at least 1")
    private double minPerDayRate;

    @Min(value = 1, message = "Maximum per day rate must be at least 1")
    private double maxPerDayRate;

    @Min(value = 0, message = "Free KM per day cannot be negative")
    private double freeKmPerDay;

    @Min(value = 0, message = "Extra KM rate cannot be negative")
    private double extraKmRate;

    @DecimalMin(value = "0.00", message = "Commission percentage cannot be negative")
    @DecimalMax(value = "100.00", message = "Commission percentage cannot exceed 100")
    private BigDecimal platformCommissionPercent = new BigDecimal("20.00");

    @DecimalMin(value = "0.00", message = "Minimum platform fee cannot be negative")
    private BigDecimal minPlatformFee = BigDecimal.ZERO;

    @DecimalMin(value = "0.00", message = "Maximum platform fee cannot be negative")
    private BigDecimal maxPlatformFee;

    @Min(value = 1, message = "Minimum booking duration must be at least 1 day")
    private Integer minBookingDurationDays = 1;

    @Min(value = 1, message = "Maximum booking duration must be at least 1 day")
    private Integer maxBookingDurationDays = 30;

    private Boolean active = true;
}
