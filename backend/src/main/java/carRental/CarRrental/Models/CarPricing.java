package carRental.CarRrental.Models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "car_pricing", indexes = {
        @Index(name = "idx_car_pricing_class_active", columnList = "carClass, active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarPricing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CarClass carClass;

    // ── Legacy / Booking fields preserved for backward compatibility ──
    @Column(nullable = false)
    private double baseFare;

    @Column(nullable = false)
    private double perDayRate;

    @Column(nullable = false)
    private double minPerDayRate;

    @Column(nullable = false)
    private double maxPerDayRate;

    @Column(nullable = false)
    private double freeKmPerDay;

    @Column(nullable = false)
    private double extraKmRate;

    // ── Marketplace BigDecimal Pricing & Commission fields ──
    @Column(name = "platform_commission_percent", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal platformCommissionPercent = new BigDecimal("20.00");

    @Column(name = "min_platform_fee", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal minPlatformFee = BigDecimal.ZERO;

    @Column(name = "max_platform_fee", precision = 10, scale = 2)
    private BigDecimal maxPlatformFee;

    @Column(name = "min_booking_duration_days")
    @Builder.Default
    private Integer minBookingDurationDays = 1;

    @Column(name = "max_booking_duration_days")
    @Builder.Default
    private Integer maxBookingDurationDays = 30;

    @Column(name = "active")
    @Builder.Default
    private Boolean active = true;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    public void onCreate() {
        Instant now = Instant.now();
        if (this.createdAt == null) this.createdAt = now;
        this.updatedAt = now;
        if (this.active == null) this.active = true;
        if (this.platformCommissionPercent == null) this.platformCommissionPercent = new BigDecimal("20.00");
        if (this.minPlatformFee == null) this.minPlatformFee = BigDecimal.ZERO;
        if (this.minBookingDurationDays == null) this.minBookingDurationDays = 1;
        if (this.maxBookingDurationDays == null) this.maxBookingDurationDays = 30;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public BigDecimal getMinDailyPriceBd() {
        return BigDecimal.valueOf(this.minPerDayRate);
    }

    public BigDecimal getMaxDailyPriceBd() {
        return BigDecimal.valueOf(this.maxPerDayRate);
    }

    public boolean isActiveRule() {
        return this.active == null || Boolean.TRUE.equals(this.active);
    }
}