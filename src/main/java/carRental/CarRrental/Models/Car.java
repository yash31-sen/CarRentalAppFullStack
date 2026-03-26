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

    // business identity (optional but nice)
    @Column(nullable = false, unique = true)
    private String carNumber; // e.g., "MP09AB1234"

    @Column(nullable = false)
    private String brand; // e.g., "Maruti"

    @Column(nullable = false)
    private String model; // e.g., "Swift"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CarClass carClass;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FuelType fuelType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransmissionType transmission;

    @Column(nullable = false)
    private double pricePerDay;

    @Column(nullable = false)
    private String currentLocation; // e.g., "Indore"

    @Column(nullable = false)
    private boolean active = true; // admin can disable car
}
