package carRental.CarRrental.Dtos;

import carRental.CarRrental.Models.HostVerificationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HostVerificationRequest {
    @NotNull(message = "Verification status is required")
    private HostVerificationStatus status;
    private String reason;
}
