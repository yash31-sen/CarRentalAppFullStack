package carRental.CarRrental.Dtos;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminIssueDto {
    private Long id;
    private Long bookingId;
    private String issueType;
    private String title;
    private String description;
    private String status;
    private String city;
    private String reportedByName;
    private String reportedByEmail;
    private String reportedUserName;
    private Long assignedSupportAgentId;
    private String assignedSupportAgentName;
    private String internalRemarks;
    private String resolutionDetails;
    private Instant escalatedAt;
    private Instant resolvedAt;
    private Instant createdAt;
}
