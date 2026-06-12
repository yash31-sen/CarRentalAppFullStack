package carRental.CarRrental.Repositories;

import carRental.CarRrental.Models.ServiceCity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceCityRepository
        extends JpaRepository<ServiceCity, Long> {

    // check if city exists and is active
    boolean existsByCityNameAndActiveTrue(String cityName);

    // find city by name
    Optional<ServiceCity> findByCityName(String cityName);

    // get all active cities
    List<ServiceCity> findByActiveTrue();

    // find city by admin
    Optional<ServiceCity> findByAdmin_Id(Long adminId);
}