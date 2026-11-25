package carRental.CarRrental.Dtos;

import carRental.CarRrental.Models.UserRole;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminSummaryDto {
    private Long id;
    private String name;
    private String email;
    private UserRole role;
    private String city;
    private List<String> assignedCities;
    private boolean active;
    private boolean emailVerified;
    private Instant createdAt;
}
