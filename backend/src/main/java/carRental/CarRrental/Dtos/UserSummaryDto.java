package carRental.CarRrental.Dtos;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSummaryDto {
    private Long id;
    private String name;
    private String email;
    private boolean active;
    private boolean suspended;
    private String suspensionReason;
    private boolean emailVerified;
    private Instant createdAt;
}
