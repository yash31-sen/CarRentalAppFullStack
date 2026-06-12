package carRental.CarRrental.Dtos;

import carRental.CarRrental.Models.DriverStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DriverUpdateRequest {
    private String name;           // update AppUser
    private String email;          // optional (usually avoid changing email; but ok)
    private String password;       // optional (admin reset)

    private String licenseNumber;  // profile
    private Integer experienceYears;
    private String currentLocation;
    private DriverStatus status;
    private Boolean active;
}
