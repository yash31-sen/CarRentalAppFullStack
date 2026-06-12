package carRental.CarRrental.Models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "car")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String carNumber;           // "MP09AB1234"

    @Column(nullable = false)
    private String brand;               // "Maruti"

    @Column(nullable = false)
    private String model;               // "Swift"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CarClass carClass;          // ECONOMY / SUV / LUXURY

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FuelType fuelType;          // PETROL / DIESEL / ELECTRIC

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransmissionType transmission; // MANUAL / AUTOMATIC

    @Column(nullable = false)
    private double pricePerDay;

    // ── Location fields ─────────────────
    @Column(nullable = false)
    private String homeCity;            // 👈 NEW
    // "Indore" permanent base
    // never changes

    @Column(nullable = false)
    private String currentCity;         // 👈 NEW
    // "Bhopal" where it is now
    // changes after one way trip

    @Column(nullable = false)
    private String currentLocation;     // exact parking address
    // "Phoenix Mall, Indore"

    // ── Status fields ───────────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CarStatus status            // 👈 NEW
            = CarStatus.AVAILABLE;      // AVAILABLE / BOOKED /
    // MAINTENANCE / DISABLED /
    // NEEDS_REPOSITIONING /
    // PENDING_INSPECTION

    @Column(nullable = false)
    private boolean active = true;      // quick on/off switch

    // ── Ownership fields ────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "added_by")
    private AppUser addedBy;            // 👈 NEW
    // admin who registered car
    // never changes

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_admin_id")
    private AppUser currentAdmin;       // 👈 NEW
    // admin currently responsible
    // changes after one way trip
}