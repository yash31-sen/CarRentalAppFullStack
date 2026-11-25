package carRental.CarRrental.Dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ServiceCityRequest {

    @NotBlank(message = "City name is required")
    @Size(min = 2, max = 50, message = "City name must be between 2 and 50 characters")
    private String cityName;

    @NotBlank(message = "Parking address is required")
    private String parkingAddress;

    @NotBlank(message = "Parking contact is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Contact must be exactly 10 digits")
    private String parkingContact;

    @Min(value = 0, message = "One-way fee cannot be negative")
    private Double oneWayFee;

    @DecimalMin(value = "0.00", message = "Local service charge cannot be negative")
    private BigDecimal localServiceCharge = BigDecimal.ZERO;

    private Boolean bookingAvailable = true;

    private Boolean active = true;

    // Optional: a city may exist without an assigned Admin
    private Long adminId;
}
