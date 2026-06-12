package carRental.CarRrental.Dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    private String name;
    private String email;
    private String password;

    public enum BookingType {
        SELF_DRIVE,
        WITH_DRIVER
    }
}
