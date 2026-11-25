package carRental.CarRrental.Dtos;

import carRental.CarRrental.Models.UserRole;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffSummaryDto {
    private Long id;
    private String name;
    private String email;
    private UserRole role;
    private String city;
    private String operationalScope;
    private boolean active;
    private boolean emailVerified;
    private Instant createdAt;
}
