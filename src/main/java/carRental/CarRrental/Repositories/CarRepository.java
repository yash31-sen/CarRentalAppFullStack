package carRental.CarRrental.Repositories;

import carRental.CarRrental.Models.Car;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CarRepository extends JpaRepository<Car, Long> {
    Optional<Car> findByCarNumber(String carNumber);
    boolean existsByCarNumber(String carNumber);
}
