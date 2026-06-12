package carRental.CarRrental.Dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceCityRequest {

    private String cityName;        // "Indore"

    private String parkingAddress;  // "Phoenix Mall, Indore"

    private String parkingContact;  // "9876543210"

    private Double oneWayFee;       // ₹500 (optional, defaults to 500)

    private Long adminId;           // which admin manages this city
}