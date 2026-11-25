package carRental.CarRrental.Services;

import carRental.CarRrental.Models.AppUser;
import carRental.CarRrental.Models.ServiceCity;
import carRental.CarRrental.Models.UserRole;
import carRental.CarRrental.Repositories.AppUserRepository;
import carRental.CarRrental.Repositories.ServiceCityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminAuthorizationService {

    private final AppUserRepository userRepository;
    private final ServiceCityRepository serviceCityRepository;

    public AppUser getAuthenticatedAdmin(String email) {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
        if (user.getRole() != UserRole.ADMIN && user.getRole() != UserRole.SUPER_ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: Admin or Super Admin role required");
        }
        return user;
    }

    public boolean isSuperAdmin(AppUser user) {
        return user.getRole() == UserRole.SUPER_ADMIN;
    }

    /**
     * Resolves all cities assigned to an Admin (via ServiceCity.admin assignment and/or AppUser.city).
     * Super Admin has global access and returns all registered service cities.
     */
    public List<String> getAssignedCities(AppUser user) {
        if (isSuperAdmin(user)) {
            return serviceCityRepository.findAll().stream()
                    .map(ServiceCity::getCityName)
                    .distinct()
                    .sorted()
                    .toList();
        }

        Set<String> cities = new LinkedHashSet<>();

        // 1. Cities assigned via Super Admin's ServiceCity assignment
        serviceCityRepository.findAllByAdmin_Id(user.getId())
                .forEach(c -> {
                    if (c.getCityName() != null && !c.getCityName().isBlank()) {
                        cities.add(c.getCityName().trim());
                    }
                });

        // 2. Cities stored on AppUser.city (supports single or comma-separated)
        if (user.getCity() != null && !user.getCity().isBlank()) {
            Arrays.stream(user.getCity().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .forEach(cities::add);
        }

        return List.copyOf(cities);
    }

    public List<String> getAuthorizedUpperCities(AppUser user, String requestedCity) {
        List<String> assigned = getAssignedCities(user);
        if (!isSuperAdmin(user) && assigned.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No cities assigned to this Admin account");
        }

        if (requestedCity != null && !requestedCity.isBlank()) {
            assertCityAuthorized(user, requestedCity);
            return List.of(requestedCity.trim().toUpperCase());
        }

        return assigned.stream().map(String::toUpperCase).toList();
    }

    public void assertCityAuthorized(AppUser user, String targetCity) {
        if (isSuperAdmin(user)) {
            return;
        }
        if (targetCity == null || targetCity.isBlank()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Resource has no assigned city; access denied");
        }
        List<String> assigned = getAssignedCities(user);
        boolean authorized = assigned.stream().anyMatch(c -> c.equalsIgnoreCase(targetCity.trim()));
        if (!authorized) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Access denied: You are not authorized to manage city '" + targetCity + "'"
            );
        }
    }
}
