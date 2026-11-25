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

    List<Car> findByAddedBy_Id(Long adminId);

    List<Car> findByCarClass(carRental.CarRrental.Models.CarClass carClass);

    List<Car> findByHost_Id(Long hostId);

    long countByApprovalStatus(carRental.CarRrental.Models.CarApprovalStatus approvalStatus);

    long countByStatusAndActiveTrue(CarStatus status);

    long countByCurrentCityIgnoreCaseAndActiveTrue(String city);

    @org.springframework.data.jpa.repository.Query("SELECT c FROM Car c LEFT JOIN FETCH c.host h LEFT JOIN FETCH h.user WHERE UPPER(c.currentCity) IN :cities ORDER BY c.id DESC")
    List<Car> findByCurrentCityInWithHost(@org.springframework.data.repository.query.Param("cities") List<String> upperCities);

    @org.springframework.data.jpa.repository.Query("SELECT c FROM Car c LEFT JOIN FETCH c.host h LEFT JOIN FETCH h.user ORDER BY c.id DESC")
    List<Car> findAllWithHost();
}