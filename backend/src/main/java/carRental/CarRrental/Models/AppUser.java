package carRental.CarRrental.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "app_user", indexes = {
        @Index(name = "idx_app_user_role", columnList = "role"),
        @Index(name = "idx_app_user_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "mobile_number")
    private String mobileNumber;

    @Column(name = "mobile_verified")
    @Builder.Default
    private Boolean mobileVerified = false;

    @JsonIgnore
    @Column(nullable = false, name = "password_hash")
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserRole role = UserRole.USER;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "app_user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    @Builder.Default
    private Set<UserRole> roles = new HashSet<>();

    @Column(name = "token_version")
    @Builder.Default
    private Integer tokenVersion = 0;

    @Column(name = "terms_accepted")
    @Builder.Default
    private Boolean termsAccepted = false;

    @Column(nullable = false, name = "email_verified")
    @Builder.Default
    private boolean emailVerified = false;

    @Column(nullable = false, name = "is_active")
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "city")
    private String city;

    @Column(name = "operational_scope")
    private String operationalScope;

    @Column(name = "suspended")
    @Builder.Default
    private Boolean suspended = false;

    @Column(name = "suspension_reason")
    private String suspensionReason;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    public void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.suspended == null) {
            this.suspended = false;
        }
        if (this.mobileVerified == null) {
            this.mobileVerified = false;
        }
        if (this.tokenVersion == null) {
            this.tokenVersion = 0;
        }
        if (this.roles == null) {
            this.roles = new HashSet<>();
        }
        if (this.role != null) {
            this.roles.add(this.role);
        }
    }

    public Set<UserRole> getEffectiveRoles() {
        Set<UserRole> effective = new HashSet<>();
        if (this.role != null) effective.add(this.role);
        if (this.roles != null) effective.addAll(this.roles);
        return effective;
    }

    public boolean hasRole(UserRole target) {
        return getEffectiveRoles().contains(target);
    }

    public void grantRole(UserRole newRole) {
        if (this.roles == null) this.roles = new HashSet<>();
        if (this.role != null) this.roles.add(this.role);
        this.roles.add(newRole);
    }

    public void incrementTokenVersion() {
        this.tokenVersion = (this.tokenVersion == null ? 0 : this.tokenVersion) + 1;
    }

    public boolean isSuspendedFlag() {
        return Boolean.TRUE.equals(this.suspended);
    }
}