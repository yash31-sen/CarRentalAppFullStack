package carRental.CarRrental.Models;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "host_profile", indexes = {
        @Index(name = "idx_host_verification_status", columnList = "verification_status"),
        @Index(name = "idx_host_city", columnList = "city")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HostProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private AppUser user;

    @Column(name = "business_name")
    private String businessName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "city")
    private String city;

    @Column(name = "address")
    private String address;

    @Column(name = "state")
    private String state;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "pan_number")
    private String panNumber;

    @Column(name = "pan_verified")
    @Builder.Default
    private Boolean panVerified = false;

    @Column(name = "profile_photo_url")
    private String profilePhotoUrl;

    @Column(name = "description", length = 1500)
    private String description;

    @Column(name = "terms_accepted")
    @Builder.Default
    private Boolean termsAccepted = false;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "government_id_number")
    private String governmentIdNumber;

    @Column(name = "document_type")
    private String documentType;

    @Column(name = "document_url")
    private String documentUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    @Builder.Default
    private HostVerificationStatus verificationStatus = HostVerificationStatus.PENDING;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "verified_by")
    private String verifiedBy;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "suspended")
    @Builder.Default
    private Boolean suspended = false;

    @Column(name = "suspension_reason")
    private String suspensionReason;

    @Column(name = "migrated_from_driver")
    @Builder.Default
    private Boolean migratedFromDriver = false;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    public void onCreate() {
        Instant now = Instant.now();
        if (this.createdAt == null) this.createdAt = now;
        this.updatedAt = now;
        if (this.suspended == null) this.suspended = false;
        if (this.migratedFromDriver == null) this.migratedFromDriver = false;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
