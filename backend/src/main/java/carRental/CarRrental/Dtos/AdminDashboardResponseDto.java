package carRental.CarRrental.Dtos;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardResponseDto {
    private String adminName;
    private String adminEmail;
    private String adminRole;
    private List<String> assignedCities;

    private long totalHosts;
    private long verifiedHosts;
    private long pendingHostApprovals;

    private long totalListedVehicles;
    private long availableVehicles;
    private long bookedVehicles;
    private long pendingVehicleApprovals;

    private long activeBookings;
    private long pendingLicenceVerifications;
    private long pendingInspections;

    private List<ActionItem> actionItems;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ActionItem {
        private String type;
        private String title;
        private String description;
        private long count;
        private String severity; // warning, info, danger, success
        private String targetTab;
        private String targetSubTab;
    }
}
