package carRental.CarRrental.Models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "driver_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Driver login identity
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private AppUser user;

    @Column(nullable = false, unique = true)
    private String licenseNumber;

    @Column(nullable = false)
    private int experienceYears;

    @Column(nullable = false)
    private String currentLocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DriverStatus status = DriverStatus.AVAILABLE;

    @Column(nullable = false)
    private boolean active = true;
}