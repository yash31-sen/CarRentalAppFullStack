package carRental.CarRrental.Dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceCityRequest {

    @NotBlank(message = "City name is required")
    @Size(min = 2, max = 50, message = "City name must be between 2 and 50 characters")
    private String cityName;        // "Indore"

    @NotBlank(message = "Parking address is required")
    private String parkingAddress;  // "Phoenix Mall, Indore"

    @NotBlank(message = "Parking contact is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Contact must be exactly 10 digits")
    private String parkingContact;  // "9876543210"

    @Min(value = 0, message = "One-way fee cannot be negative")
    private Double oneWayFee;       // ₹500 (optional, defaults to 500)

    @NotNull(message = "Admin ID is required")
    private Long adminId;           // which admin manages this city
}