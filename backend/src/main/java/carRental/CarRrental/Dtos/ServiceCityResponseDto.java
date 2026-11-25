package carRental.CarRrental.Dtos;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceCityResponseDto {
    private Long id;
    private String cityName;
    private String parkingAddress;
    private String parkingContact;
    private double oneWayFee;
    private BigDecimal localServiceCharge;
    private boolean active;
    private boolean bookingAvailable;
    private AdminSummaryDto admin;
    private long hostCount;
    private long vehicleCount;
    private long activeBookingCount;
}
