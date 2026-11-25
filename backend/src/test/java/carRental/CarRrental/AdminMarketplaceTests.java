package carRental.CarRrental;

import carRental.CarRrental.Models.*;
import carRental.CarRrental.Repositories.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminMarketplaceTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private AppUserRepository userRepository;
    @Autowired private HostProfileRepository hostProfileRepository;
    @Autowired private CarRepository carRepository;
    @Autowired private CarPricingRepository carPricingRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private VehicleInspectionRepository inspectionRepository;

    private AppUser createAdmin(String email, String city) {
        return userRepository.save(AppUser.builder()
                .name("Test Admin")
                .email(email)
                .passwordHash("encoded")
                .role(UserRole.ADMIN)
                .emailVerified(true)
                .isActive(true)
                .city(city)
                .build());
    }

    private HostProfile createHost(String email, String city, HostVerificationStatus status) {
        AppUser u = userRepository.save(AppUser.builder()
                .name("Host " + city)
                .email(email)
                .passwordHash("encoded")
                .role(UserRole.HOST)
                .emailVerified(true)
                .isActive(true)
                .city(city)
                .build());
        return hostProfileRepository.save(HostProfile.builder()
                .user(u)
                .city(city)
                .phone("9999999999")
                .governmentIdNumber("ABCD123456")
                .verificationStatus(status)
                .active(true)
                .build());
    }

    @Test
    void unauthenticatedAccessToAdminIsRejected() throws Exception {
        mockMvc.perform(get("/admin/dashboard/stats"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = "USER")
    void userCannotAccessAdminApis() throws Exception {
        mockMvc.perform(get("/admin/hosts"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "host@test.com", roles = "HOST")
    void hostCannotAccessAdminApis() throws Exception {
        mockMvc.perform(get("/admin/cars"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin.indore@test.com", roles = "ADMIN")
    void adminCannotAccessSuperAdminApisOrBypassCityRestriction() throws Exception {
        createAdmin("admin.indore@test.com", "Indore");

        mockMvc.perform(get("/super-admin/settings"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/admin/hosts").param("city", "Bhopal"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin.indore@test.com", roles = "ADMIN")
    void adminCanManageAssignedCityHostsAndIsBlockedFromOtherCities() throws Exception {
        createAdmin("admin.indore@test.com", "Indore");
        HostProfile indoreHost = createHost("host.indore@test.com", "Indore", HostVerificationStatus.PENDING);
        HostProfile bhopalHost = createHost("host.bhopal@test.com", "Bhopal", HostVerificationStatus.PENDING);

        mockMvc.perform(get("/admin/hosts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + indoreHost.getId() + ")]").exists())
                .andExpect(jsonPath("$[?(@.id == " + bhopalHost.getId() + ")]").doesNotExist());

        mockMvc.perform(patch("/admin/hosts/" + indoreHost.getId() + "/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationStatus").value("VERIFIED"));

        mockMvc.perform(patch("/admin/hosts/" + bhopalHost.getId() + "/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Invalid ID\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin.indore@test.com", roles = "ADMIN")
    void adminVehicleWorkflowEnforcesPricingAndBlocksDirectCreationOrOverride() throws Exception {
        createAdmin("admin.indore@test.com", "Indore");
        HostProfile indoreHost = createHost("host.indore2@test.com", "Indore", HostVerificationStatus.VERIFIED);
        HostProfile bhopalHost = createHost("host.bhopal2@test.com", "Bhopal", HostVerificationStatus.VERIFIED);

        if (!carPricingRepository.existsByCarClassAndActiveTrue(CarClass.HATCHBACK)) {
            carPricingRepository.save(CarPricing.builder()
                    .carClass(CarClass.HATCHBACK)
                    .baseFare(100)
                    .perDayRate(1500)
                    .minPerDayRate(1000)
                    .maxPerDayRate(2000)
                    .freeKmPerDay(100)
                    .extraKmRate(12)
                    .active(true)
                    .build());
        }

        Car validCar = carRepository.save(Car.builder()
                .carNumber("MP09TEST01")
                .brand("Maruti")
                .model("Swift")
                .carClass(CarClass.HATCHBACK)
                .fuelType(FuelType.PETROL)
                .transmission(TransmissionType.MANUAL)
                .pricePerDay(1500)
                .homeCity("Indore")
                .currentCity("Indore")
                .currentLocation("Vijay Nagar")
                .status(CarStatus.AVAILABLE)
                .approvalStatus(CarApprovalStatus.PENDING)
                .host(indoreHost)
                .active(false)
                .build());

        Car invalidPriceCar = carRepository.save(Car.builder()
                .carNumber("MP09TEST02")
                .brand("Hyundai")
                .model("i20")
                .carClass(CarClass.HATCHBACK)
                .fuelType(FuelType.PETROL)
                .transmission(TransmissionType.MANUAL)
                .pricePerDay(500) // Below ₹1000 min
                .homeCity("Indore")
                .currentCity("Indore")
                .currentLocation("Vijay Nagar")
                .status(CarStatus.AVAILABLE)
                .approvalStatus(CarApprovalStatus.PENDING)
                .host(indoreHost)
                .active(false)
                .build());

        Car bhopalCar = carRepository.save(Car.builder()
                .carNumber("MP04TEST03")
                .brand("Tata")
                .model("Altroz")
                .carClass(CarClass.HATCHBACK)
                .fuelType(FuelType.PETROL)
                .transmission(TransmissionType.MANUAL)
                .pricePerDay(1400)
                .homeCity("Bhopal")
                .currentCity("Bhopal")
                .currentLocation("MP Nagar")
                .status(CarStatus.AVAILABLE)
                .approvalStatus(CarApprovalStatus.PENDING)
                .host(bhopalHost)
                .active(false)
                .build());

        // Cannot create or override price
        mockMvc.perform(post("/admin/cars").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/admin/cars/" + validCar.getId()).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());

        // Valid vehicle approves
        mockMvc.perform(patch("/admin/cars/" + validCar.getId() + "/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvalStatus").value("APPROVED"));

        // Invalid category pricing rejected
        mockMvc.perform(patch("/admin/cars/" + invalidPriceCar.getId() + "/approve"))
                .andExpect(status().isBadRequest());

        // Reject with reason works
        mockMvc.perform(patch("/admin/cars/" + invalidPriceCar.getId() + "/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Price below category minimum\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvalStatus").value("REJECTED"));

        // Unauthorized city vehicle blocked
        mockMvc.perform(get("/admin/cars/" + bhopalCar.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin.indore@test.com", roles = "ADMIN")
    void adminBookingAndInspectionOperationsEnforceSelfDriveAndCityIsolation() throws Exception {
        createAdmin("admin.indore@test.com", "Indore");
        HostProfile indoreHost = createHost("host.indore3@test.com", "Indore", HostVerificationStatus.VERIFIED);
        AppUser customer = userRepository.save(AppUser.builder()
                .name("Customer")
                .email("cust@test.com")
                .passwordHash("encoded")
                .role(UserRole.USER)
                .emailVerified(true)
                .isActive(true)
                .build());

        Car car = carRepository.save(Car.builder()
                .carNumber("MP09TEST99")
                .brand("Honda")
                .model("City")
                .carClass(CarClass.SEDAN)
                .fuelType(FuelType.PETROL)
                .transmission(TransmissionType.AUTOMATIC)
                .pricePerDay(2000)
                .homeCity("Indore")
                .currentCity("Indore")
                .currentLocation("Vijay Nagar")
                .status(CarStatus.AVAILABLE)
                .approvalStatus(CarApprovalStatus.APPROVED)
                .host(indoreHost)
                .active(true)
                .build());

        Booking booking = bookingRepository.save(Booking.builder()
                .user(customer)
                .car(car)
                .bookingType(BookingType.SELF_DRIVE)
                .tripType(TripType.ROUND_TRIP)
                .status(BookingStatus.VERIFICATION_PENDING)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .pickupCity("Indore")
                .dropCity("Indore")
                .pickupLocation("Vijay Nagar")
                .dropLocation("Vijay Nagar")
                .baseFare(0)
                .basePrice(4000)
                .oneWayFee(0)
                .estimatedPrice(4000)
                .extraKmCharge(0)
                .finalPrice(4000)
                .totalPrice(4000)
                .grossAmount(new BigDecimal("4000.00"))
                .platformCommissionPercent(new BigDecimal("20.00"))
                .platformCommissionAmount(new BigDecimal("800.00"))
                .hostEarningsAmount(new BigDecimal("3200.00"))
                .drivingLicenseNumber("MP0920200012345")
                .build());

        // Driver assignment blocked
        mockMvc.perform(post("/admin/bookings/" + booking.getId() + "/assign-driver/1"))
                .andExpect(status().isForbidden());

        // Verify licence works and preserves historical snapshot
        mockMvc.perform(post("/admin/bookings/" + booking.getId() + "/verify-license")
                        .param("approved", "true")
                        .param("remarks", "Valid licence"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        mockMvc.perform(get("/admin/bookings/" + booking.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.platformCommissionAmount").value(800.00))
                .andExpect(jsonPath("$.hostEarningsAmount").value(3200.00));

        // Inspection oversight + Fleet Manager execution block
        VehicleInspection insp = inspectionRepository.save(VehicleInspection.builder()
                .car(car)
                .inspectionType(InspectionType.INITIAL)
                .status(InspectionStatus.PENDING)
                .build());

        mockMvc.perform(get("/admin/inspections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + insp.getId() + ")]").exists());

        mockMvc.perform(patch("/admin/inspections/" + insp.getId() + "/escalate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"remarks\":\"Needs urgent brake check\",\"suspendVehicle\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ESCALATED"));

        mockMvc.perform(post("/admin/inspections/" + insp.getId() + "/execute"))
                .andExpect(status().isForbidden());
    }
}
