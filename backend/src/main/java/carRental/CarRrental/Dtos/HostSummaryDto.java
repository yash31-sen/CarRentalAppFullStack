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
public class HostSummaryDto {
    private Long id;
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private String city;
    private String businessName;
    private HostVerificationStatus verificationStatus;
    private String rejectionReason;
    private boolean active;
    private boolean suspended;
    private boolean emailVerified;
    private long vehicleCount;
    private Instant createdAt;
}
