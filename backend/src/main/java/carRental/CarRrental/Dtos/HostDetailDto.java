package carRental.CarRrental.Dtos;

import carRental.CarRrental.Models.HostVerificationStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HostDetailDto {
    private Long id;
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private String city;
    private String address;
    private String businessName;
    private String governmentIdNumber;
    private String documentType;
    private String documentUrl;
    private HostVerificationStatus verificationStatus;
    private String rejectionReason;
    private String verifiedBy;
    private Instant verifiedAt;
    private boolean active;
    private boolean suspended;
    private String suspensionReason;
    private boolean emailVerified;
    private Instant createdAt;

    private long totalBookings;
    private long completedBookings;
    private BigDecimal totalEarnings;
    private List<VehicleItem> vehicles;
    private List<UserDetailDto.BookingSummaryItem> bookingHistory;
    private List<PayoutItem> payoutHistory;
    private List<UserDetailDto.ReviewItem> reviews;
    private List<UserDetailDto.IncidentItem> reportedIncidents;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class VehicleItem {
        private Long id;
        private String carNumber;
        private String brand;
        private String model;
        private String carClass;
        private double pricePerDay;
        private String status;
        private String approvalStatus;
        private boolean pricingOutOfRange;
        private boolean active;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PayoutItem {
        private Long id;
        private Long bookingId;
        private BigDecimal amount;
        private String status;
        private String payoutRef;
        private Instant createdAt;
        private Instant paidAt;
    }
}
