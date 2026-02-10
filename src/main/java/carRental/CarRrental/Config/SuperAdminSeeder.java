package carRental.CarRrental.Config;

import carRental.CarRrental.Models.AppUser;
import carRental.CarRrental.Models.UserRole;
import carRental.CarRrental.Repositories.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SuperAdminSeeder implements CommandLineRunner {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SuperAdminSeeder(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        String email = "superadmin@carrental.com";

        if (!userRepository.existsByEmail(email)) {
            AppUser superAdmin = AppUser.builder()
                    .name("Super Admin")
                    .email(email)
                    .passwordHash(passwordEncoder.encode("SuperAdmin@123"))
                    .role(UserRole.SUPER_ADMIN)
                    .emailVerified(true)
                    .build();

            userRepository.save(superAdmin);
            System.out.println("✅ SUPER_ADMIN seeded: " + email);
        }
    }
}
