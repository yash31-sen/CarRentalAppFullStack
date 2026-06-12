package carRental.CarRrental.Repositories;

import carRental.CarRrental.Models.Car;
import carRental.CarRrental.Models.CarStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CarRepository extends JpaRepository<Car, Long> {

    Optional<Car> findByCarNumber(String carNumber);

    boolean existsByCarNumber(String carNumber);

    // 👇 NEW - search available cars by city
    List<Car> findByCurrentCityAndStatusAndActiveTrue(
            String currentCity,
            CarStatus status
    );

    // 👇 NEW - get all cars by city
    List<Car> findByCurrentCityAndActiveTrue(String currentCity);

    // 👇 NEW - get cars needing repositioning
    List<Car> findByStatus(CarStatus status);

    // 👇 NEW - get cars by admin
    List<Car> findByCurrentAdmin_Id(Long adminId);
}