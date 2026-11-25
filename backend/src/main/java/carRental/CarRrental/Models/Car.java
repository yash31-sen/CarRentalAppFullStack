package carRental.CarRrental.Models;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

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

    // ── Marketplace Host & Approval fields ──
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id")
    private HostProfile host;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status")
    @Builder.Default
    private CarApprovalStatus approvalStatus = CarApprovalStatus.APPROVED;

    @Column(name = "pricing_out_of_range")
    @Builder.Default
    private Boolean pricingOutOfRange = false;

    @Column(name = "manufacturing_year")
    private Integer manufacturingYear;

    @Column(name = "seating_capacity")
    private Integer seatingCapacity;

    @Column(length = 1500)
    private String description;

    @Column(name = "rc_document_url")
    private String rcDocumentUrl;

    @Column(name = "insurance_document_url")
    private String insuranceDocumentUrl;

    @Column(name = "puc_document_url")
    private String pucDocumentUrl;

    @Column(name = "other_document_url")
    private String otherDocumentUrl;

    @Column(name = "photo_urls", length = 2000)
    private String photoUrls;

    @Column(name = "admin_remarks", length = 1000)
    private String adminRemarks;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @PrePersist
    public void onCreate() {
        if (this.approvalStatus == null) {
            this.approvalStatus = CarApprovalStatus.APPROVED;
        }
        if (this.pricingOutOfRange == null) {
            this.pricingOutOfRange = false;
        }
        if (this.submittedAt == null) {
            this.submittedAt = Instant.now();
        }
    }
}