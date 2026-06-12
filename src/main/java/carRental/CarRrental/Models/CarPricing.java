package carRental.CarRrental.Models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "car_pricing")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarPricing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // one pricing rule per car class
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private CarClass carClass;          // ECONOMY / SUV / LUXURY

    // ── Time component ──────────────────
    @Column(nullable = false)
    private double baseFare;            // ₹200 flat booking fee
    // charged regardless of
    // days or km

    @Column(nullable = false)
    private double perDayRate;          // ₹1500 default per day
    // admin can change within
    // min-max range

    @Column(nullable = false)
    private double minPerDayRate;       // ₹1200 floor
    // admin cannot go below

    @Column(nullable = false)
    private double maxPerDayRate;       // ₹1800 ceiling
    // admin cannot go above

    // ── Distance component ──────────────
    @Column(nullable = false)
    private double freeKmPerDay;        // 100 km free per day
    // not charged extra

    @Column(nullable = false)
    private double extraKmRate;         // ₹12/km after free km
    // charged at trip end
}