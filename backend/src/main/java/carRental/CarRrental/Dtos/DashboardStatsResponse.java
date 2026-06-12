package carRental.CarRrental.Dtos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsResponse {

    private long totalAdmins;

    private long totalUsers;

    private long totalDrivers;

    private long totalCities;

    private long totalCars;

    private long totalBookings;

    private double totalRevenue;
}