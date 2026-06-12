package carRental.CarRrental.Models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "app_user")
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

    @Column(nullable = false, name = "password_hash")
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.USER;

    @Column(nullable = false, name = "email_verified")
    private boolean emailVerified = false;

    @Column(nullable = false, name = "is_active")
    public boolean isActive = true;

    @Column(name = "city")
    private String city;                // 👈 NEW
    // nullable because
    // USER and DRIVER
    // don't need a city
}