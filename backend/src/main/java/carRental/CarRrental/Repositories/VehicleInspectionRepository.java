package carRental.CarRrental.Repositories;

import carRental.CarRrental.Models.InspectionStatus;
import carRental.CarRrental.Models.VehicleInspection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VehicleInspectionRepository extends JpaRepository<VehicleInspection, Long> {

    @Query("SELECT v FROM VehicleInspection v JOIN FETCH v.car c LEFT JOIN FETCH c.host LEFT JOIN FETCH v.fleetManager WHERE UPPER(c.currentCity) IN :cities ORDER BY v.createdAt DESC")
    List<VehicleInspection> findByCarCityIn(@Param("cities") List<String> upperCities);

    @Query("SELECT v FROM VehicleInspection v JOIN FETCH v.car c LEFT JOIN FETCH c.host LEFT JOIN FETCH v.fleetManager ORDER BY v.createdAt DESC")
    List<VehicleInspection> findAllWithDetails();

    List<VehicleInspection> findByCar_IdOrderByCreatedAtDesc(Long carId);

    List<VehicleInspection> findByBooking_IdOrderByCreatedAtDesc(Long bookingId);

    long countByStatusAndCar_CurrentCityIgnoreCase(InspectionStatus status, String city);
}
