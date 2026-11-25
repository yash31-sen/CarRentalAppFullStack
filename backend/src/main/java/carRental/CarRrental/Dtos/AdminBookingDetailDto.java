package carRental.CarRrental.Dtos;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminBookingDetailDto {
    private Long id;
    private String bookingType;
    private String tripType;
    private String status;
    private String paymentStatus;
    private String paymentRef;
    private Instant paymentAt;

    // Customer
    private Long customerId;
    private String customerName;
    private String customerEmail;

    // Host
    private Long hostId;
    private String hostName;
    private String hostEmail;
    private String hostPhone;

    // Vehicle
    private Long carId;
    private String vehicleName;
    private String vehicleNumber;
    private String carClass;

    // Schedule & Route
    private LocalDate startDate;
    private LocalDate endDate;
    private String pickupCity;
    private String dropCity;
    private String pickupLocation;
    private String dropLocation;

    // Pricing & Immutable Commission Snapshot
    private double baseFare;
    private double basePrice;
    private double oneWayFee;
    private double extraKmCharge;
    private double finalPrice;
    private BigDecimal grossAmount;
    private BigDecimal platformCommissionPercent;
    private BigDecimal platformCommissionAmount;
    private BigDecimal platformFeeAmount;
    private BigDecimal hostEarningsAmount;

    // Licence
    private String drivingLicenseNumber;
    private String licenceDocumentUrl;
    private String licenceRemarks;
    private Instant licenceSubmittedAt;
    private Instant licenceVerifiedAt;
    private String licenceVerifiedBy;

    private Instant createdAt;
    private Instant updatedAt;

    private List<AdminInspectionDto> inspections;
    private List<AdminIssueDto> relatedIssues;
}
