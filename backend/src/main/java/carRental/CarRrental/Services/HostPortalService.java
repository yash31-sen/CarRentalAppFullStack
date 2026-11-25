package carRental.CarRrental.Services;

import carRental.CarRrental.Models.*;
import carRental.CarRrental.Repositories.BookingRepository;
import carRental.CarRrental.Repositories.CarRepository;
import carRental.CarRrental.Repositories.HostProfileRepository;
import carRental.CarRrental.Repositories.ServiceCityRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HostPortalService {

    private final HostProfileRepository hostProfileRepository;
    private final CarRepository carRepository;
    private final BookingRepository bookingRepository;
    private final ServiceCityRepository serviceCityRepository;
    private final PricingValidationService pricingValidationService;
    private final AuditLogService auditLogService;

    public HostPortalService(
            HostProfileRepository hostProfileRepository,
            CarRepository carRepository,
            BookingRepository bookingRepository,
            ServiceCityRepository serviceCityRepository,
            PricingValidationService pricingValidationService,
            AuditLogService auditLogService
    ) {
        this.hostProfileRepository = hostProfileRepository;
        this.carRepository = carRepository;
        this.bookingRepository = bookingRepository;
        this.serviceCityRepository = serviceCityRepository;
        this.pricingValidationService = pricingValidationService;
        this.auditLogService = auditLogService;
    }

    public Map<String, Object> getDashboard(String hostEmail) {
        HostProfile host = requireVerifiedHost(hostEmail);
        List<Car> cars = carRepository.findByHost_Id(host.getId());
        List<Booking> bookings = bookingRepository.findByCar_Host_IdOrderByCreatedAtDesc(host.getId());
        BigDecimal totalEarnings = bookingRepository.getTotalHostEarnings(host.getId());

        long availableCars = cars.stream().filter(c -> c.isActive() && c.getStatus() == CarStatus.AVAILABLE).count();
        long pendingApprovalCars = cars.stream().filter(c -> c.getApprovalStatus() == CarApprovalStatus.PENDING).count();
        long bookedCars = cars.stream().filter(c -> c.getStatus() == CarStatus.BOOKED).count();
        long activeBookings = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED
                        || b.getStatus() == BookingStatus.IN_PROGRESS
                        || b.getStatus() == BookingStatus.PAYMENT_SUCCESS
                        || b.getStatus() == BookingStatus.VERIFICATION_PENDING)
                .count();
        long completedBookings = bookings.stream().filter(b -> b.getStatus() == BookingStatus.COMPLETED).count();

        Map<String, Object> res = new HashMap<>();
        res.put("hostId", host.getId());
        res.put("hostName", host.getUser().getName());
        res.put("businessName", host.getBusinessName());
        res.put("city", host.getCity());
        res.put("verificationStatus", host.getVerificationStatus().name());
        res.put("totalVehicles", cars.size());
        res.put("availableVehicles", availableCars);
        res.put("pendingApprovalVehicles", pendingApprovalCars);
        res.put("bookedVehicles", bookedCars);
        res.put("totalBookings", bookings.size());
        res.put("activeBookings", activeBookings);
        res.put("completedBookings", completedBookings);
        res.put("totalEarnings", totalEarnings != null ? totalEarnings : BigDecimal.ZERO);
        return res;
    }

    public List<Map<String, Object>> getMyVehicles(String hostEmail) {
        HostProfile host = requireVerifiedHost(hostEmail);
        return carRepository.findByHost_Id(host.getId()).stream()
                .map(this::toVehicleMap)
                .toList();
    }

    @Transactional
    public Map<String, Object> createVehicle(String hostEmail, Map<String, Object> payload) {
        HostProfile host = requireVerifiedHost(hostEmail);

        String carNumber = getRequiredString(payload, "carNumber").toUpperCase();
        if (carRepository.existsByCarNumber(carNumber)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A vehicle with this registration number already exists.");
        }

        String brand = getRequiredString(payload, "brand");
        String model = getRequiredString(payload, "model");
        CarClass carClass = CarClass.valueOf(getRequiredString(payload, "carClass").toUpperCase());
        FuelType fuelType = FuelType.valueOf(getRequiredString(payload, "fuelType").toUpperCase());
        TransmissionType transmission = TransmissionType.valueOf(getRequiredString(payload, "transmission").toUpperCase());
        double pricePerDay = Double.parseDouble(String.valueOf(payload.get("pricePerDay")));

        String city = payload.get("city") != null && !String.valueOf(payload.get("city")).isBlank()
                ? String.valueOf(payload.get("city")).trim()
                : host.getCity();
        if (!serviceCityRepository.existsByCityNameIgnoreCaseAndActiveTrue(city)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected city is not an active service city.");
        }

        String location = payload.get("currentLocation") != null && !String.valueOf(payload.get("currentLocation")).isBlank()
                ? String.valueOf(payload.get("currentLocation")).trim()
                : (host.getAddress() != null ? host.getAddress() : city);

        pricingValidationService.validateHostVehiclePrice(carClass, BigDecimal.valueOf(pricePerDay));

        Car car = Car.builder()
                .carNumber(carNumber)
                .brand(brand)
                .model(model)
                .carClass(carClass)
                .fuelType(fuelType)
                .transmission(transmission)
                .pricePerDay(pricePerDay)
                .homeCity(city)
                .currentCity(city)
                .currentLocation(location)
                .status(CarStatus.PENDING_INSPECTION)
                .approvalStatus(CarApprovalStatus.PENDING)
                .active(false)
                .host(host)
                .addedBy(host.getUser())
                .build();

        if (payload.get("manufacturingYear") != null) {
            car.setManufacturingYear(Integer.parseInt(String.valueOf(payload.get("manufacturingYear"))));
        }

        Car saved = carRepository.save(car);
        auditLogService.log("HOST_ADD_VEHICLE", "Car", String.valueOf(saved.getId()),
                "Host " + hostEmail + " submitted vehicle " + saved.getCarNumber());
        return toVehicleMap(saved);
    }

    @Transactional
    public Map<String, Object> toggleVehicleAvailability(String hostEmail, Long carId, boolean active) {
        HostProfile host = requireVerifiedHost(hostEmail);
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found"));

        if (car.getHost() == null || !car.getHost().getId().equals(host.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this vehicle.");
        }
        if (car.getApprovalStatus() != CarApprovalStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vehicle must be approved before changing availability.");
        }

        car.setActive(active);
        if (active && car.getStatus() == CarStatus.DISABLED) {
            car.setStatus(CarStatus.AVAILABLE);
        } else if (!active && car.getStatus() == CarStatus.AVAILABLE) {
            car.setStatus(CarStatus.DISABLED);
        }
        return toVehicleMap(carRepository.save(car));
    }

    public List<Map<String, Object>> getMyBookings(String hostEmail) {
        HostProfile host = requireVerifiedHost(hostEmail);
        return bookingRepository.findByCar_Host_IdOrderByCreatedAtDesc(host.getId()).stream()
                .map(b -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", b.getId());
                    item.put("carNumber", b.getCar().getCarNumber());
                    item.put("carTitle", b.getCar().getBrand() + " " + b.getCar().getModel());
                    item.put("customerName", b.getUser().getName());
                    item.put("pickupCity", b.getPickupCity());
                    item.put("startDate", b.getStartDate());
                    item.put("endDate", b.getEndDate());
                    item.put("status", b.getStatus().name());
                    item.put("finalPrice", b.getFinalPrice());
                    item.put("hostEarningsAmount", b.getHostEarningsAmount() != null ? b.getHostEarningsAmount() : BigDecimal.ZERO);
                    item.put("createdAt", b.getCreatedAt());
                    return item;
                })
                .toList();
    }

    public Map<String, Object> getEarningsSummary(String hostEmail) {
        HostProfile host = requireVerifiedHost(hostEmail);
        List<Map<String, Object>> bookings = getMyBookings(hostEmail);
        BigDecimal totalEarnings = bookingRepository.getTotalHostEarnings(host.getId());
        return Map.of(
                "totalEarnings", totalEarnings != null ? totalEarnings : BigDecimal.ZERO,
                "bookingsCount", bookings.size(),
                "transactions", bookings
        );
    }

    private HostProfile requireVerifiedHost(String hostEmail) {
        HostProfile host = hostProfileRepository.findByUser_Email(hostEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Host profile not found."));

        boolean isApprovedStatus = host.getVerificationStatus() == HostVerificationStatus.VERIFIED
                || host.getVerificationStatus() == HostVerificationStatus.APPROVED;
        if (!isApprovedStatus || !host.isActive() || Boolean.TRUE.equals(host.getSuspended())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Your Host account is not verified or is inactive.");
        }
        return host;
    }

    private Map<String, Object> toVehicleMap(Car car) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", car.getId());
        m.put("carNumber", car.getCarNumber());
        m.put("brand", car.getBrand());
        m.put("model", car.getModel());
        m.put("carClass", car.getCarClass().name());
        m.put("fuelType", car.getFuelType().name());
        m.put("transmission", car.getTransmission().name());
        m.put("pricePerDay", car.getPricePerDay());
        m.put("city", car.getCurrentCity());
        m.put("currentLocation", car.getCurrentLocation());
        m.put("status", car.getStatus().name());
        m.put("approvalStatus", car.getApprovalStatus() != null ? car.getApprovalStatus().name() : "PENDING");
        m.put("active", car.isActive());
        m.put("manufacturingYear", car.getManufacturingYear());
        return m;
    }

    private String getRequiredString(Map<String, Object> payload, String key) {
        Object val = payload.get(key);
        if (val == null || String.valueOf(val).isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, key + " is required.");
        }
        return String.valueOf(val).trim();
    }
}
