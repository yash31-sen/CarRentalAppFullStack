package carRental.CarRrental.Dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DriverCreateRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "License number is required")
    @Pattern(regexp = "^[A-Z0-9]{5,20}$", message = "Invalid license number format")
    private String licenseNumber;

    @Min(value = 0, message = "Experience years cannot be negative")
    private int experienceYears;

    @NotBlank(message = "Current location is required")
    private String currentLocation;
}
