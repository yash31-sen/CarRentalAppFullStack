package carRental.CarRrental.Dtos;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminCarListingDto {
    private Long id;
    private String carNumber;
    private String brand;
    private String model;
    private String carClass;
    private String fuelType;
    private String transmission;
    private double pricePerDay;
    private String currentCity;
    private String currentLocation;
    private String approvalStatus;
    private String status;
    private boolean active;
    private boolean pricingOutOfRange;
    private String pricingViolationMessage;
    private Long hostId;
    private String hostName;
    private String hostPhone;
    private String hostEmail;
    private Instant submittedAt;
}
