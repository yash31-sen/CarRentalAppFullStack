package carRental.CarRrental.Models;

import carRental.CarRrental.Dtos.RegisterRequest;
import carRental.CarRrental.Dtos.ResetPasswordRequest;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "booking")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // who booked
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    // which car
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    // driver optional (only when WITH_DRIVER)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_profile_id")
    private DriverProfile driverProfile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegisterRequest.BookingType bookingType;        // SELF_DRIVE / WITH_DRIVER

    // 👇 NEW
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TripType tripType;              // ROUND_TRIP / ONE_WAY

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    // dates
    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    // 👇 NEW - city level
    @Column(nullable = false)
    private String pickupCity;              // "Indore"

    @Column(nullable = false)
    private String dropCity;               // "Bhopal" or "Indore"

    // address level
    @Column(nullable = false)
    private String pickupLocation;          // "Indore Railway Station"

    @Column(nullable = false)
    private String dropLocation;            // "Bhopal Bus Stand"

    // pricing
    @Column(nullable = false)
    private double basePrice;               // 👇 NEW days × pricePerDay

    @Column(nullable = false)
    private double oneWayFee;               // 👇 NEW 0 if round trip

    @Column(nullable = false)
    private double totalPrice;              // basePrice + oneWayFee

    // payment
    @Column(unique = true)
    private String paymentRef;
    private Instant paymentAt;

    // self drive licence
    private String drivingLicenseNumber;
    private Instant licenceSubmittedAt;
    private Instant licenceVerifiedAt;
    private String licenceVerifiedBy;

    // admin workload
    private Long assignedAdminId;
    private Instant assignedAt;

    // 👇 NEW - trip timestamps
    private Instant tripStartedAt;          // when driver started trip
    private Instant tripEndedAt;            // when trip completed

    // booking audit
    @Column(nullable = false)
    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();      // 👈 fix this
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();      // 👈 fix this
    }

    // ── Odometer readings ───────────────
    @Column(name = "start_odometer")
    private Double startOdometer;       // km reading at trip start
    // submitted by driver

    @Column(name = "end_odometer")
    private Double endOdometer;         // km reading at trip end
    // submitted by driver

    @Column(name = "total_km_driven")
    private Double totalKmDriven;       // endOdometer - startOdometer
    // calculated by system

    // ── Odometer photo proof ────────────
    @Column(name = "start_odometer_photo")
    private String startOdometerPhoto;  // photo url at trip start

    @Column(name = "end_odometer_photo")
    private String endOdometerPhoto;    // photo url at trip end

    // ── Pricing breakdown ───────────────
    @Column(nullable = false)
    private double baseFare;            // flat fee

    @Column(nullable = false)
    private double estimatedPrice;      // charged at booking
    // baseFare
    // + days × perDayRate
    // + oneWayFee

    @Column(nullable = false)
    private double extraKmCharge;       // calculated after trip
    // 0 at booking time
    // updated after trip ends

    @Column(nullable = false)
    private double finalPrice;          // estimatedPrice
    // + extraKmCharge
    // updated after trip ends
}