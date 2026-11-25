package carRental.CarRrental.Models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "platform_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "default_platform_commission_percent", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal defaultPlatformCommissionPercent = new BigDecimal("20.00");

    @Column(name = "default_platform_service_fee", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal defaultPlatformServiceFee = new BigDecimal("99.00");

    @Column(name = "min_booking_duration_days", nullable = false)
    @Builder.Default
    private Integer minBookingDurationDays = 1;

    @Column(name = "max_booking_duration_days", nullable = false)
    @Builder.Default
    private Integer maxBookingDurationDays = 30;

    @Column(name = "cancellation_policy", length = 2000)
    @Builder.Default
    private String cancellationPolicy = "Free cancellation up to 24 hours before pickup. 50% fee within 24 hours.";

    @Column(name = "refund_policy", length = 2000)
    @Builder.Default
    private String refundPolicy = "Refunds are processed within 5-7 business days to the original payment method.";

    @Column(name = "host_verification_required", nullable = false)
    @Builder.Default
    private boolean hostVerificationRequired = true;

    @Column(name = "guest_verification_required", nullable = false)
    @Builder.Default
    private boolean guestVerificationRequired = true;

    @Column(name = "vehicle_listing_approval_required", nullable = false)
    @Builder.Default
    private boolean vehicleListingApprovalRequired = true;

    @Column(name = "one_way_booking_enabled", nullable = false)
    @Builder.Default
    private boolean oneWayBookingEnabled = true;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @PrePersist
    @PreUpdate
    public void onSave() {
        this.updatedAt = Instant.now();
    }
}
