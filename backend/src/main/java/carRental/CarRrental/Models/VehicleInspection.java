package carRental.CarRrental.Models;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "vehicle_inspection", indexes = {
        @Index(name = "idx_inspection_car", columnList = "car_id"),
        @Index(name = "idx_inspection_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleInspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fleet_manager_id")
    private AppUser fleetManager;

    @Enumerated(EnumType.STRING)
    @Column(name = "inspection_type", nullable = false)
    private InspectionType inspectionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private InspectionStatus status = InspectionStatus.PENDING;

    @Column(name = "result")
    private String result;

    @Column(name = "damage_details", length = 2000)
    private String damageDetails;

    @Column(name = "photo_urls", length = 2000)
    private String photoUrls;

    @Column(name = "fleet_manager_remarks", length = 1500)
    private String fleetManagerRemarks;

    @Column(name = "escalated_by_admin")
    private String escalatedByAdmin;

    @Column(name = "escalation_remarks", length = 1500)
    private String escalationRemarks;

    @Column(name = "scheduled_date")
    private Instant scheduledDate;

    @Column(name = "completed_date")
    private Instant completedDate;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    public void onCreate() {
        if (this.createdAt == null) this.createdAt = Instant.now();
    }
}
