package carRental.CarRrental.Dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminActionReasonRequest {
    private String reason;
    private String remarks;
    private Boolean suspendVehicle;
    private Long supportAgentId;
    private String resolutionDetails;
    private String status;
}
