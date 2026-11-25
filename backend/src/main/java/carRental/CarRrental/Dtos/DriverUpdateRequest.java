package carRental.CarRrental.Dtos;

import carRental.CarRrental.Models.DriverStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DriverUpdateRequest {

    private String name;
    private String email;
    private String password;
    private String licenseNumber;
    private Integer experienceYears;
    private String currentLocation;
    private DriverStatus status;
    private Boolean active;
}
