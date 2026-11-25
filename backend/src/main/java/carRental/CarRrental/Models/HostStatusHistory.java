package carRental.CarRrental.Models;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "host_status_history", indexes = {
        @Index(name = "idx_host_status_history_host", columnList = "host_profile_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HostStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "host_profile_id", nullable = false)
    private HostProfile hostProfile;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status")
    private HostVerificationStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    private HostVerificationStatus newStatus;

    @Column(name = "changed_by", nullable = false)
    private String changedBy;

    @Column(length = 1000)
    private String remarks;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    public void onCreate() {
        if (this.createdAt == null) this.createdAt = Instant.now();
    }
}
