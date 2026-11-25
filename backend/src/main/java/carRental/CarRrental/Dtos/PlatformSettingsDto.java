package carRental.CarRrental.Dtos;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformSettingsDto {

    @NotNull(message = "Default platform commission is required")
    @DecimalMin(value = "0.00", message = "Commission cannot be negative")
    @DecimalMax(value = "100.00", message = "Commission cannot exceed 100%")
    private BigDecimal defaultPlatformCommissionPercent;

    @NotNull(message = "Default platform service fee is required")
    @DecimalMin(value = "0.00", message = "Service fee cannot be negative")
    private BigDecimal defaultPlatformServiceFee;

    @NotNull(message = "Minimum booking duration is required")
    @Min(value = 1, message = "Minimum booking duration must be at least 1 day")
    private Integer minBookingDurationDays;

    @NotNull(message = "Maximum booking duration is required")
    @Min(value = 1, message = "Maximum booking duration must be at least 1 day")
    private Integer maxBookingDurationDays;

    @NotBlank(message = "Cancellation policy is required")
    private String cancellationPolicy;

    @NotBlank(message = "Refund policy is required")
    private String refundPolicy;

    private boolean hostVerificationRequired;
    private boolean guestVerificationRequired;
    private boolean vehicleListingApprovalRequired;
    private boolean oneWayBookingEnabled;

    private Instant updatedAt;
    private String updatedBy;
}
