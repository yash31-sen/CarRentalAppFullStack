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
public class UserDetailDto {
    private Long id;
    private String name;
    private String email;
    private boolean active;
    private boolean suspended;
    private String suspensionReason;
    private boolean emailVerified;
    private Instant createdAt;

    private List<BookingSummaryItem> bookingHistory;
    private List<PaymentHistoryItem> paymentHistory;
    private List<ReviewItem> reviews;
    private List<IncidentItem> reportedIncidents;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class BookingSummaryItem {
        private Long id;
        private String carName;
        private String carNumber;
        private String status;
        private LocalDate startDate;
        private LocalDate endDate;
        private String pickupCity;
        private String dropCity;
        private BigDecimal amount;
        private Instant createdAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PaymentHistoryItem {
        private Long bookingId;
        private String paymentRef;
        private BigDecimal amount;
        private String status;
        private Instant paymentAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ReviewItem {
        private Long id;
        private String reviewerName;
        private int rating;
        private String comment;
        private Instant createdAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class IncidentItem {
        private Long id;
        private String title;
        private String description;
        private String status;
        private String reportedByName;
        private Instant createdAt;
    }
}
