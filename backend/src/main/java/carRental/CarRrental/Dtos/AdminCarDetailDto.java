package carRental.CarRrental.Dtos;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminCarDetailDto {
    private Long id;
    private String carNumber;
    private String brand;
    private String model;
    private Integer manufacturingYear;
    private String carClass;
    private String fuelType;
    private String transmission;
    private Integer seatingCapacity;
    private double pricePerDay;
    private Double minAllowedPrice;
    private Double maxAllowedPrice;
    private boolean pricingOutOfRange;
    private String pricingViolationMessage;
    private String description;
    private String currentCity;
    private String currentLocation;
    private String approvalStatus;
    private String status;
    private boolean active;
    private String adminRemarks;
    private Instant submittedAt;

    // Documents & Photos
    private String rcDocumentUrl;
    private String insuranceDocumentUrl;
    private String pucDocumentUrl;
    private String otherDocumentUrl;
    private List<String> photoUrls;

    // Host info
    private Long hostId;
    private String hostName;
    private String hostEmail;
    private String hostPhone;
    private String hostVerificationStatus;
    private boolean hostActive;

    // Histories
    private List<ListingHistoryItem> listingHistory;
    private List<AdminInspectionDto> inspectionHistory;
    private List<UserDetailDto.BookingSummaryItem> bookingHistory;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ListingHistoryItem {
        private Long id;
        private String action;
        private String actorEmail;
        private String remarks;
        private Instant createdAt;
    }
}
