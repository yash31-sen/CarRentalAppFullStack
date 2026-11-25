package carRental.CarRrental.Dtos;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminInspectionDto {
    private Long id;
    private Long carId;
    private String carNumber;
    private String vehicleName;
    private String city;
    private String hostName;
    private Long bookingId;
    private String inspectionType;
    private Long fleetManagerId;
    private String fleetManagerName;
    private String status;
    private String result;
    private String damageDetails;
    private List<String> photoUrls;
    private String fleetManagerRemarks;
    private String escalatedByAdmin;
    private String escalationRemarks;
    private Instant scheduledDate;
    private Instant completedDate;
    private Instant createdAt;
}
