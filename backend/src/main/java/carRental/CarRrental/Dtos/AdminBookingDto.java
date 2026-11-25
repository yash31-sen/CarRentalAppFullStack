package carRental.CarRrental.Dtos;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminBookingDto {
    private Long id;
    private String customerName;
    private String customerEmail;
    private String customerContact;
    private String vehicleName;
    private String vehicleNumber;
    private String carClass;
    private String hostName;
    private String hostPhone;
    private LocalDate startDate;
    private LocalDate endDate;
    private String pickupCity;
    private String dropCity;
    private String pickupLocation;
    private BigDecimal totalAmount;
    private String paymentStatus;
    private String bookingStatus;
    private String bookingType;
    private String drivingLicenseNumber;
    private String licenceDocumentUrl;
    private Instant createdAt;
}
