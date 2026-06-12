package carRental.CarRrental.Repositories;

import carRental.CarRrental.Models.CarClass;
import carRental.CarRrental.Models.CarPricing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface CarPricingRepository
        extends JpaRepository<CarPricing, Long> {

    // find pricing rule for a car class
    Optional<CarPricing> findByCarClass(CarClass carClass);

    // check if pricing exists for a car class
    boolean existsByCarClass(CarClass carClass);

    // get all pricing rules
    List<CarPricing> findAll();
}