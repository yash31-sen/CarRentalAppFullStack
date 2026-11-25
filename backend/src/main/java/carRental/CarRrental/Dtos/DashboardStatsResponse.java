package carRental.CarRrental.Dtos;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsResponse {

    // Primary marketplace statistics
    private long totalAdmins;
    private long totalUsers;
    private long totalHosts;
    private long totalCities;
    private long totalListedCars;
    private long totalBookings;
    private BigDecimal totalRevenue;
    private BigDecimal platformEarnings;

    // Additional operational statistics
    private long pendingHostVerifications;
    private long pendingVehicleApprovals;
    private long activeBookings;
    private long availableCars;
    private long unavailableCars;
    private long pendingHostPayouts;
    private BigDecimal pendingHostPayoutAmount;

    // Backward-compatible aliases
    public long getTotalDrivers() {
        return totalHosts;
    }

    public long getTotalCars() {
        return totalListedCars;
    }
}
