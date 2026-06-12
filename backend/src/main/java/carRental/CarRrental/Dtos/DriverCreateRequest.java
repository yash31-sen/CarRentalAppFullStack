package carRental.CarRrental.Dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DriverCreateRequest {
    private String name;
    private String email;
    private String password;

    private String licenseNumber;
    private int experienceYears;
    private String currentLocation;
}
