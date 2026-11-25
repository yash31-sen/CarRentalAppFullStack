package carRental.CarRrental.Services;

import carRental.CarRrental.Dtos.*;
import carRental.CarRrental.Models.*;
import carRental.CarRrental.Repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminMarketplaceService {

    private final AdminAuthorizationService authService;
    private final AppUserRepository userRepository;
    private final HostProfileRepository hostProfileRepository;
    private final CarRepository carRepository;
    private final CarPricingRepository carPricingRepository;
    private final BookingRepository bookingRepository;
    private final HostPayoutRepository hostPayoutRepository;
    private final ReviewRepository reviewRepository;
    private final IncidentReportRepository incidentReportRepository;
    private final VehicleInspectionRepository inspectionRepository;
    private final ListingHistoryRepository listingHistoryRepository;
    private final HostStatusHistoryRepository hostStatusHistoryRepository;
    private final PricingValidationService pricingValidationService;
    private final EmailService emailService;
    private final AuditLogService auditLogService;

    // =============================================
    // 1. CITY OPERATIONS DASHBOARD
    // =============================================

    public AdminDashboardResponseDto getDashboardStats(String adminEmail, String requestedCity) {
        AppUser admin = authService.getAuthenticatedAdmin(adminEmail);
        List<String> assignedCities = authService.getAssignedCities(admin);
        List<String> upperCities = authService.getAuthorizedUpperCities(admin, requestedCity);

        List<HostProfile> hosts = hostProfileRepository.findAllWithUser().stream()
                .filter(h -> h.getCity() != null && upperCities.contains(h.getCity().trim().toUpperCase()))
                .toList();

        List<Car> cars = carRepository.findByCurrentCityInWithHost(upperCities);
        List<Booking> bookings = bookingRepository.findSelfDriveBookingsByCities(upperCities);
        List<VehicleInspection> inspections = inspectionRepository.findByCarCityIn(upperCities);
        List<IncidentReport> openIssues = getCityIssuesInternal(upperCities).stream()
                .filter(i -> i.getStatus() != IncidentStatus.RESOLVED)
                .toList();

        long verifiedHosts = hosts.stream()
                .filter(h -> h.getVerificationStatus() == HostVerificationStatus.VERIFIED
                        || h.getVerificationStatus() == HostVerificationStatus.APPROVED)
                .count();
        long pendingHostApprovals = hosts.stream()
                .filter(h -> h.getVerificationStatus() == HostVerificationStatus.PENDING
                        || h.getVerificationStatus() == HostVerificationStatus.PENDING_ADMIN_APPROVAL
                        || h.getVerificationStatus() == HostVerificationStatus.RESUBMISSION_REQUIRED
                        || h.getVerificationStatus() == HostVerificationStatus.RESUBMISSION_REQUESTED)
                .count();

        long availableVehicles = cars.stream().filter(c -> c.isActive() && c.getStatus() == CarStatus.AVAILABLE).count();
        long bookedVehicles = cars.stream().filter(c -> c.getStatus() == CarStatus.BOOKED).count();
        long pendingVehicleApprovals = cars.stream()
                .filter(c -> c.getApprovalStatus() == CarApprovalStatus.PENDING)
                .count();
        long resubmittedVehicles = cars.stream()
                .filter(c -> c.getApprovalStatus() == CarApprovalStatus.RESUBMISSION_REQUESTED)
                .count();

        long activeBookings = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED
                        || b.getStatus() == BookingStatus.IN_PROGRESS
                        || b.getStatus() == BookingStatus.PAYMENT_SUCCESS
                        || b.getStatus() == BookingStatus.VERIFICATION_PENDING)
                .count();
        long pendingLicences = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.VERIFICATION_PENDING)
                .count();

        long pendingInspections = inspections.stream()
                .filter(i -> i.getStatus() == InspectionStatus.PENDING || i.getStatus() == InspectionStatus.SCHEDULED)
                .count();
        long damageOrFailedInspections = inspections.stream()
                .filter(i -> i.getStatus() == InspectionStatus.FAILED
                        || (i.getDamageDetails() != null && !i.getDamageDetails().isBlank() && i.getStatus() != InspectionStatus.ESCALATED))
                .count();

        List<AdminDashboardResponseDto.ActionItem> actionItems = new ArrayList<>();
        if (pendingHostApprovals > 0) {
            actionItems.add(AdminDashboardResponseDto.ActionItem.builder()
                    .type("HOST_VERIFICATION")
                    .title("New Host Verification Requests")
                    .description(pendingHostApprovals + " host verification(s) awaiting review in your city.")
                    .count(pendingHostApprovals)
                    .severity("warning")
                    .targetTab("hosts")
                    .build());
        }
        if (pendingVehicleApprovals > 0) {
            actionItems.add(AdminDashboardResponseDto.ActionItem.builder()
                    .type("VEHICLE_APPROVAL")
                    .title("Pending Vehicle Listing Approvals")
                    .description(pendingVehicleApprovals + " host-submitted vehicle listing(s) awaiting approval.")
                    .count(pendingVehicleApprovals)
                    .severity("info")
                    .targetTab("cars")
                    .build());
        }
        if (resubmittedVehicles > 0) {
            actionItems.add(AdminDashboardResponseDto.ActionItem.builder()
                    .type("VEHICLE_RESUBMISSION")
                    .title("Vehicle Resubmissions")
                    .description(resubmittedVehicles + " vehicle listing(s) marked for resubmission review.")
                    .count(resubmittedVehicles)
                    .severity("warning")
                    .targetTab("cars")
                    .build());
        }
        if (pendingLicences > 0) {
            actionItems.add(AdminDashboardResponseDto.ActionItem.builder()
                    .type("LICENCE_VERIFICATION")
                    .title("Pending Customer Driving Licence Verifications")
                    .description(pendingLicences + " self-drive booking(s) awaiting licence verification.")
                    .count(pendingLicences)
                    .severity("warning")
                    .targetTab("bookings")
                    .targetSubTab("verification")
                    .build());
        }
        if (pendingInspections > 0) {
            actionItems.add(AdminDashboardResponseDto.ActionItem.builder()
                    .type("INSPECTION_PENDING")
                    .title("Vehicles Awaiting Inspection")
                    .description(pendingInspections + " vehicle(s) pending Fleet Manager inspection.")
                    .count(pendingInspections)
                    .severity("info")
                    .targetTab("inspections")
                    .build());
        }
        if (damageOrFailedInspections > 0) {
            actionItems.add(AdminDashboardResponseDto.ActionItem.builder()
                    .type("DAMAGE_REPORT")
                    .title("Vehicle Damage / Failed Inspection Reports")
                    .description(damageOrFailedInspections + " inspection(s) reported damage or failure.")
                    .count(damageOrFailedInspections)
                    .severity("danger")
                    .targetTab("inspections")
                    .build());
        }
        if (!openIssues.isEmpty()) {
            actionItems.add(AdminDashboardResponseDto.ActionItem.builder()
                    .type("BOOKING_ISSUE")
                    .title("Open Booking Disputes & Escalations")
                    .description(openIssues.size() + " booking issue(s) or dispute(s) require attention.")
                    .count(openIssues.size())
                    .severity("danger")
                    .targetTab("bookings")
                    .targetSubTab("issues")
                    .build());
        }

        return AdminDashboardResponseDto.builder()
                .adminName(admin.getName())
                .adminEmail(admin.getEmail())
                .adminRole(admin.getRole().name())
                .assignedCities(assignedCities)
                .totalHosts(hosts.size())
                .verifiedHosts(verifiedHosts)
                .pendingHostApprovals(pendingHostApprovals)
                .totalListedVehicles(cars.size())
                .availableVehicles(availableVehicles)
                .bookedVehicles(bookedVehicles)
                .pendingVehicleApprovals(pendingVehicleApprovals)
                .activeBookings(activeBookings)
                .pendingLicenceVerifications(pendingLicences)
                .pendingInspections(pendingInspections)
                .actionItems(actionItems)
                .build();
    }

    // =============================================
    // 2. VEHICLE LISTINGS MODULE
    // =============================================

    public List<AdminCarListingDto> getCityVehicles(String adminEmail,
                                                    String city,
                                                    CarApprovalStatus approvalStatus,
                                                    CarStatus status,
                                                    CarClass category,
                                                    String search) {
        AppUser admin = authService.getAuthenticatedAdmin(adminEmail);
        List<String> upperCities = authService.getAuthorizedUpperCities(admin, city);

        return carRepository.findByCurrentCityInWithHost(upperCities).stream()
                .filter(c -> approvalStatus == null || c.getApprovalStatus() == approvalStatus)
                .filter(c -> status == null || c.getStatus() == status)
                .filter(c -> category == null || c.getCarClass() == category)
                .filter(c -> {
                    if (search == null || search.isBlank()) return true;
                    String q = search.trim().toLowerCase();
                    boolean mPlate = c.getCarNumber() != null && c.getCarNumber().toLowerCase().contains(q);
                    boolean mModel = (c.getBrand() + " " + c.getModel()).toLowerCase().contains(q);
                    boolean mHost = c.getHost() != null && c.getHost().getUser().getName().toLowerCase().contains(q);
                    return mPlate || mModel || mHost;
                })
                .map(this::toCarListingDto)
                .toList();
    }

    public AdminCarDetailDto getVehicleDetails(Long carId, String adminEmail) {
        AppUser admin = authService.getAuthenticatedAdmin(adminEmail);
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found"));
        authService.assertCityAuthorized(admin, car.getCurrentCity());

        CarPricing pricing = carPricingRepository.findFirstByCarClassAndActiveTrue(car.getCarClass()).orElse(null);
        String violationMsg = checkPricingViolation(car, pricing);

        List<AdminCarDetailDto.ListingHistoryItem> history = listingHistoryRepository.findByCar_IdOrderByCreatedAtDesc(carId)
                .stream()
                .map(h -> AdminCarDetailDto.ListingHistoryItem.builder()
                        .id(h.getId())
                        .action(h.getAction())
                        .actorEmail(h.getActorEmail())
                        .remarks(h.getRemarks())
                        .createdAt(h.getCreatedAt())
                        .build())
                .toList();

        List<AdminInspectionDto> inspections = inspectionRepository.findByCar_IdOrderByCreatedAtDesc(carId)
                .stream()
                .map(this::toInspectionDto)
                .toList();

        List<UserDetailDto.BookingSummaryItem> bookings = bookingRepository.findByCar_IdOrderByCreatedAtDesc(carId)
                .stream()
                .map(b -> UserDetailDto.BookingSummaryItem.builder()
                        .id(b.getId())
                        .carName(car.getBrand() + " " + car.getModel())
                        .carNumber(car.getCarNumber())
                        .status(b.getStatus().name())
                        .startDate(b.getStartDate())
                        .endDate(b.getEndDate())
                        .pickupCity(b.getPickupCity())
                        .dropCity(b.getDropCity())
                        .amount(b.getGrossAmount() != null ? b.getGrossAmount() : BigDecimal.valueOf(b.getFinalPrice()))
                        .createdAt(b.getCreatedAt())
                        .build())
                .toList();

        List<String> photos = (car.getPhotoUrls() != null && !car.getPhotoUrls().isBlank())
                ? Arrays.stream(car.getPhotoUrls().split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList()
                : Collections.emptyList();

        HostProfile host = car.getHost();
        return AdminCarDetailDto.builder()
                .id(car.getId())
                .carNumber(car.getCarNumber())
                .brand(car.getBrand())
                .model(car.getModel())
                .manufacturingYear(car.getManufacturingYear())
                .carClass(car.getCarClass().name())
                .fuelType(car.getFuelType().name())
                .transmission(car.getTransmission().name())
                .seatingCapacity(car.getSeatingCapacity())
                .pricePerDay(car.getPricePerDay())
                .minAllowedPrice(pricing != null ? pricing.getMinPerDayRate() : null)
                .maxAllowedPrice(pricing != null ? pricing.getMaxPerDayRate() : null)
                .pricingOutOfRange(violationMsg != null || Boolean.TRUE.equals(car.getPricingOutOfRange()))
                .pricingViolationMessage(violationMsg)
                .description(car.getDescription())
                .currentCity(car.getCurrentCity())
                .currentLocation(car.getCurrentLocation())
                .approvalStatus(car.getApprovalStatus() != null ? car.getApprovalStatus().name() : "APPROVED")
                .status(car.getStatus().name())
                .active(car.isActive())
                .adminRemarks(car.getAdminRemarks())
                .submittedAt(car.getSubmittedAt())
                .rcDocumentUrl(car.getRcDocumentUrl())
                .insuranceDocumentUrl(car.getInsuranceDocumentUrl())
                .pucDocumentUrl(car.getPucDocumentUrl())
                .otherDocumentUrl(car.getOtherDocumentUrl())
                .photoUrls(photos)
                .hostId(host != null ? host.getId() : null)
                .hostName(host != null ? host.getUser().getName() : "Legacy Fleet")
                .hostEmail(host != null ? host.getUser().getEmail() : null)
                .hostPhone(host != null ? host.getPhone() : null)
                .hostVerificationStatus(host != null ? host.getVerificationStatus().name() : null)
                .hostActive(host == null || host.isActive())
                .listingHistory(history)
                .inspectionHistory(inspections)
                .bookingHistory(bookings)
                .build();
    }

    @Transactional
    public AdminCarListingDto approveVehicle(Long carId, String adminEmail, String remarks) {
        AppUser admin = authService.getAuthenticatedAdmin(adminEmail);
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found"));
        authService.assertCityAuthorized(admin, car.getCurrentCity());

        if (car.getHost() != null && car.getHost().getVerificationStatus() != HostVerificationStatus.VERIFIED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot approve vehicle: Host verification status is " + car.getHost().getVerificationStatus());
        }

        // Enforce Super Admin category pricing limits using BigDecimal
        pricingValidationService.validateHostVehiclePrice(car.getCarClass(), BigDecimal.valueOf(car.getPricePerDay()));

        car.setApprovalStatus(CarApprovalStatus.APPROVED);
        car.setPricingOutOfRange(false);
        car.setActive(true);
        if (car.getStatus() == CarStatus.DISABLED || car.getStatus() == CarStatus.SUSPENDED) {
            car.setStatus(CarStatus.AVAILABLE);
        }
        if (remarks != null && !remarks.isBlank()) {
            car.setAdminRemarks(remarks.trim());
        }

        Car saved = carRepository.save(car);
        recordListingHistory(saved, "APPROVED", adminEmail, remarks);
        auditLogService.log("APPROVE_VEHICLE", "Car", String.valueOf(carId), "Approved vehicle " + saved.getCarNumber());
        return toCarListingDto(saved);
    }

    @Transactional
    public AdminCarListingDto rejectVehicle(Long carId, String adminEmail, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rejection reason is required");
        }
        AppUser admin = authService.getAuthenticatedAdmin(adminEmail);
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found"));
        authService.assertCityAuthorized(admin, car.getCurrentCity());

        car.setApprovalStatus(CarApprovalStatus.REJECTED);
        car.setActive(false);
        car.setAdminRemarks(reason.trim());
        Car saved = carRepository.save(car);

        recordListingHistory(saved, "REJECTED", adminEmail, reason.trim());
        auditLogService.log("REJECT_VEHICLE", "Car", String.valueOf(carId), "Reason: " + reason.trim());
        return toCarListingDto(saved);
    }

    @Transactional
    public AdminCarListingDto requestVehicleResubmission(Long carId, String adminEmail, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Resubmission reason is required");
        }
        AppUser admin = authService.getAuthenticatedAdmin(adminEmail);
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found"));
        authService.assertCityAuthorized(admin, car.getCurrentCity());

        car.setApprovalStatus(CarApprovalStatus.RESUBMISSION_REQUESTED);
        car.setActive(false);
        car.setAdminRemarks(reason.trim());
        Car saved = carRepository.save(car);

        recordListingHistory(saved, "RESUBMISSION_REQUESTED", adminEmail, reason.trim());
        auditLogService.log("REQUEST_VEHICLE_RESUBMISSION", "Car", String.valueOf(carId), "Reason: " + reason.trim());
        return toCarListingDto(saved);
    }

    @Transactional
    public AdminCarListingDto suspendVehicle(Long carId, String adminEmail, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Suspension reason is required");
        }
        AppUser admin = authService.getAuthenticatedAdmin(adminEmail);
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found"));
        authService.assertCityAuthorized(admin, car.getCurrentCity());

        if (car.getStatus() == CarStatus.BOOKED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot suspend a vehicle currently on an active trip");
        }

        car.setStatus(CarStatus.SUSPENDED);
        car.setActive(false);
        car.setAdminRemarks(reason.trim());
        Car saved = carRepository.save(car);

        recordListingHistory(saved, "SUSPENDED", adminEmail, reason.trim());
        auditLogService.log("SUSPEND_VEHICLE", "Car", String.valueOf(carId), "Reason: " + reason.trim());
        return toCarListingDto(saved);
    }

    @Transactional
    public AdminCarListingDto disableVehicle(Long carId, String adminEmail, String reason) {
        AppUser admin = authService.getAuthenticatedAdmin(adminEmail);
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found"));
        authService.assertCityAuthorized(admin, car.getCurrentCity());

        if (car.getStatus() == CarStatus.BOOKED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot disable a vehicle currently on an active trip");
        }

        car.setStatus(CarStatus.DISABLED);
        car.setActive(false);
        if (reason != null && !reason.isBlank()) car.setAdminRemarks(reason.trim());
        Car saved = carRepository.save(car);

        recordListingHistory(saved, "DISABLED", adminEmail, reason);
        auditLogService.log("DISABLE_VEHICLE", "Car", String.valueOf(carId), "Vehicle disabled");
        return toCarListingDto(saved);
    }

    @Transactional
    public AdminCarListingDto reactivateVehicle(Long carId, String adminEmail) {
        AppUser admin = authService.getAuthenticatedAdmin(adminEmail);
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found"));
        authService.assertCityAuthorized(admin, car.getCurrentCity());

        if (car.getApprovalStatus() != CarApprovalStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only APPROVED vehicles can be reactivated. Current status: " + car.getApprovalStatus());
        }
        if (car.getHost() != null && (!car.getHost().isActive() || Boolean.TRUE.equals(car.getHost().getSuspended()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot reactivate vehicle while Host account is inactive or suspended");
        }
        pricingValidationService.validateHostVehiclePrice(car.getCarClass(), BigDecimal.valueOf(car.getPricePerDay()));

        car.setStatus(CarStatus.AVAILABLE);
        car.setActive(true);
        car.setPricingOutOfRange(false);
        Car saved = carRepository.save(car);

        recordListingHistory(saved, "REACTIVATED", adminEmail, "Vehicle reactivated");
        auditLogService.log("REACTIVATE_VEHICLE", "Car", String.valueOf(carId), "Vehicle reactivated");
        return toCarListingDto(saved);
    }

    // =============================================
    // 3. HOST MANAGEMENT MODULE
    // =============================================

    public List<HostSummaryDto> getCityHosts(String adminEmail,
                                             String city,
                                             HostVerificationStatus verificationStatus,
                                             String accountStatus,
                                             String search) {
        AppUser admin = authService.getAuthenticatedAdmin(adminEmail);
        List<String> upperCities = authService.getAuthorizedUpperCities(admin, city);

        return hostProfileRepository.findAllWithUser().stream()
                .filter(h -> h.getCity() != null && upperCities.contains(h.getCity().trim().toUpperCase()))
                .filter(h -> verificationStatus == null || h.getVerificationStatus() == verificationStatus)
                .filter(h -> {
                    if (accountStatus == null || accountStatus.isBlank()) return true;
                    return switch (accountStatus.toUpperCase()) {
                        case "ACTIVE" -> h.isActive() && !Boolean.TRUE.equals(h.getSuspended());
                        case "INACTIVE" -> !h.isActive() && !Boolean.TRUE.equals(h.getSuspended());
                        case "SUSPENDED" -> Boolean.TRUE.equals(h.getSuspended());
                        default -> true;
                    };
                })
                .filter(h -> {
                    if (search == null || search.isBlank()) return true;
                    String q = search.trim().toLowerCase();
                    return (h.getUser().getName() != null && h.getUser().getName().toLowerCase().contains(q))
                            || (h.getUser().getEmail() != null && h.getUser().getEmail().toLowerCase().contains(q))
                            || (h.getPhone() != null && h.getPhone().toLowerCase().contains(q))
                            || String.valueOf(h.getId()).equals(q);
                })
                .map(h -> HostSummaryDto.builder()
                        .id(h.getId())
                        .userId(h.getUser().getId())
                        .name(h.getUser().getName())
                        .email(h.getUser().getEmail())
                        .phone(h.getPhone())
                        .city(h.getCity())
                        .businessName(h.getBusinessName())
                        .verificationStatus(h.getVerificationStatus())
                        .rejectionReason(h.getRejectionReason())
                        .active(h.isActive())
                        .suspended(Boolean.TRUE.equals(h.getSuspended()))
                        .emailVerified(h.getUser().isEmailVerified())
                        .vehicleCount(carRepository.findByHost_Id(h.getId()).size())
                        .createdAt(h.getCreatedAt())
                        .build())
                .toList();
    }

    public HostDetailDto getHostDetails(Long hostId, String adminEmail) {
        AppUser admin = authService.getAuthenticatedAdmin(adminEmail);
        HostProfile host = hostProfileRepository.findById(hostId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Host not found"));
        authService.assertCityAuthorized(admin, host.getCity());

        AppUser user = host.getUser();
        List<HostDetailDto.VehicleItem> vehicles = carRepository.findByHost_Id(hostId).stream()
                .map(c -> HostDetailDto.VehicleItem.builder()
                        .id(c.getId())
                        .carNumber(c.getCarNumber())
                        .brand(c.getBrand())
                        .model(c.getModel())
                        .carClass(c.getCarClass().name())
                        .pricePerDay(c.getPricePerDay())
                        .status(c.getStatus().name())
                        .approvalStatus(c.getApprovalStatus() != null ? c.getApprovalStatus().name() : "APPROVED")
                        .pricingOutOfRange(Boolean.TRUE.equals(c.getPricingOutOfRange()))
                        .active(c.isActive())
                        .build())
                .toList();

        List<Booking> bookings = bookingRepository.findByCar_Host_IdOrderByCreatedAtDesc(hostId);
        long completedCount = bookings.stream().filter(b -> b.getStatus() == BookingStatus.COMPLETED).count();
        BigDecimal totalEarnings = bookingRepository.getTotalHostEarnings(hostId);

        List<UserDetailDto.BookingSummaryItem> bookingHistory = bookings.stream()
                .map(b -> UserDetailDto.BookingSummaryItem.builder()
                        .id(b.getId())
                        .carName(b.getCar().getBrand() + " " + b.getCar().getModel())
                        .carNumber(b.getCar().getCarNumber())
                        .status(b.getStatus().name())
                        .startDate(b.getStartDate())
                        .endDate(b.getEndDate())
                        .pickupCity(b.getPickupCity())
                        .dropCity(b.getDropCity())
                        .amount(b.getHostEarningsAmount() != null ? b.getHostEarningsAmount() : BigDecimal.ZERO)
                        .createdAt(b.getCreatedAt())
                        .build())
                .toList();

        List<HostDetailDto.PayoutItem> payouts = hostPayoutRepository.findByHostProfile_IdOrderByCreatedAtDesc(hostId).stream()
                .map(p -> HostDetailDto.PayoutItem.builder()
                        .id(p.getId())
                        .bookingId(p.getBooking() != null ? p.getBooking().getId() : null)
                        .amount(p.getAmount())
                        .status(p.getStatus().name())
                        .payoutRef(p.getPayoutRef())
                        .createdAt(p.getCreatedAt())
                        .paidAt(p.getPaidAt())
                        .build())
                .toList();

        List<UserDetailDto.ReviewItem> reviews = reviewRepository.findByTargetUserIdWithReviewer(user.getId()).stream()
                .map(r -> UserDetailDto.ReviewItem.builder()
                        .id(r.getId())
                        .reviewerName(r.getReviewer().getName())
                        .rating(r.getRating())
                        .comment(r.getComment())
                        .createdAt(r.getCreatedAt())
                        .build())
                .toList();

        List<UserDetailDto.IncidentItem> incidents = incidentReportRepository.findByReportedUserIdWithReporter(user.getId()).stream()
                .map(i -> UserDetailDto.IncidentItem.builder()
                        .id(i.getId())
                        .title(i.getTitle())
                        .description(i.getDescription())
                        .status(i.getStatus().name())
                        .reportedByName(i.getReportedBy().getName())
                        .createdAt(i.getCreatedAt())
                        .build())
                .toList();

        return HostDetailDto.builder()
                .id(host.getId())
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(host.getPhone())
                .city(host.getCity())
                .address(host.getAddress())
                .businessName(host.getBusinessName())
                .governmentIdNumber(maskIdNumber(host.getGovernmentIdNumber()))
                .documentType(host.getDocumentType())
                .documentUrl(host.getDocumentUrl())
                .verificationStatus(host.getVerificationStatus())
                .rejectionReason(host.getRejectionReason())
                .verifiedBy(host.getVerifiedBy())
                .verifiedAt(host.getVerifiedAt())
                .active(host.isActive())
                .suspended(Boolean.TRUE.equals(host.getSuspended()))
                .suspensionReason(host.getSuspensionReason())
                .emailVerified(user.isEmailVerified())
                .createdAt(host.getCreatedAt())
                .totalBookings(bookings.size())
                .completedBookings(completedCount)
                .totalEarnings(totalEarnings != null ? totalEarnings : BigDecimal.ZERO)
                .vehicles(vehicles)
                .bookingHistory(bookingHistory)
                .payoutHistory(payouts)
                .reviews(reviews)
                .reportedIncidents(incidents)
                .build();
    }

    @Transactional
    public HostSummaryDto verifyHost(Long hostId, String adminEmail, HostVerificationStatus status, String reason) {
        AppUser admin = authService.getAuthenticatedAdmin(adminEmail);
        HostProfile host = hostProfileRepository.findById(hostId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Host not found"));
        authService.assertCityAuthorized(admin, host.getCity());

        if (host.getUser().getEmail().equalsIgnoreCase(adminEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "A Host cannot approve their own verification");
        }
        if ((status == HostVerificationStatus.REJECTED
                || status == HostVerificationStatus.RESUBMISSION_REQUIRED
                || status == HostVerificationStatus.RESUBMISSION_REQUESTED)
                && (reason == null || reason.isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reason is required when rejecting or requesting resubmission");
        }

        HostVerificationStatus previousStatus = host.getVerificationStatus();
        boolean approved = status == HostVerificationStatus.VERIFIED || status == HostVerificationStatus.APPROVED;
        host.setVerificationStatus(status);
        host.setRejectionReason(approved ? null : reason.trim());
        host.setVerifiedBy(adminEmail);
        host.setVerifiedAt(Instant.now());
        if (approved) {
            host.setActive(true);
            AppUser hostUser = host.getUser();
            hostUser.grantRole(UserRole.HOST);
            hostUser.incrementTokenVersion();
            userRepository.save(hostUser);
        } else {
            host.setActive(false);
        }
        HostProfile saved = hostProfileRepository.save(host);

        hostStatusHistoryRepository.save(HostStatusHistory.builder()
                .hostProfile(saved)
                .previousStatus(previousStatus)
                .newStatus(status)
                .changedBy(adminEmail)
                .remarks(approved ? "Approved by Admin" : reason.trim())
                .build());

        emailService.sendEmail(
                saved.getUser().getEmail(),
                approved ? "Host Application Approved - Car Rental" : "Host Application Update - Car Rental",
                approved
                        ? "Hello " + saved.getUser().getName() + ",\n\nCongratulations! Your Host application for " + saved.getCity()
                        + " has been approved. Please log in again or refresh your session to access the Host Portal.\n\nBest regards,\nCar Rental Team"
                        : "Hello " + saved.getUser().getName() + ",\n\nYour Host application status has been updated to " + status.name()
                        + ".\nReason: " + reason.trim() + "\n\nBest regards,\nCar Rental Team"
        );

        auditLogService.log("ADMIN_VERIFY_HOST_" + status.name(), "HostProfile", String.valueOf(hostId),
                "Status=" + status + (reason != null ? " | Reason: " + reason : ""));

        return HostSummaryDto.builder()
                .id(saved.getId())
                .userId(saved.getUser().getId())
                .name(saved.getUser().getName())
                .email(saved.getUser().getEmail())
                .phone(saved.getPhone())
                .city(saved.getCity())
                .businessName(saved.getBusinessName())
                .verificationStatus(saved.getVerificationStatus())
                .rejectionReason(saved.getRejectionReason())
                .active(saved.isActive())
                .suspended(Boolean.TRUE.equals(saved.getSuspended()))
                .emailVerified(saved.getUser().isEmailVerified())
                .vehicleCount(carRepository.findByHost_Id(saved.getId()).size())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Transactional
    public void suspendHost(Long hostId, String adminEmail, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Suspension reason is required");
        }
        AppUser admin = authService.getAuthenticatedAdmin(adminEmail);
        HostProfile host = hostProfileRepository.findById(hostId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Host not found"));
        authService.assertCityAuthorized(admin, host.getCity());

        host.setSuspended(true);
        host.setSuspensionReason(reason.trim());
        host.setActive(false);
        host.getUser().setSuspended(true);
        host.getUser().setSuspensionReason(reason.trim());
        host.getUser().setActive(false);
        userRepository.save(host.getUser());
        hostProfileRepository.save(host);

        auditLogService.log("ADMIN_SUSPEND_HOST", "HostProfile", String.valueOf(hostId), "Reason: " + reason.trim());
    }

    @Transactional
    public void setHostActiveStatus(Long hostId, String adminEmail, boolean active) {
        AppUser admin = authService.getAuthenticatedAdmin(adminEmail);
        HostProfile host = hostProfileRepository.findById(hostId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Host not found"));
        authService.assertCityAuthorized(admin, host.getCity());

        if (active) {
            host.setSuspended(false);
            host.setSuspensionReason(null);
            host.getUser().setSuspended(false);
            host.getUser().setSuspensionReason(null);
        }
        host.setActive(active);
        host.getUser().setActive(active);
        userRepository.save(host.getUser());
        hostProfileRepository.save(host);

        auditLogService.log(active ? "ADMIN_REACTIVATE_HOST" : "ADMIN_DEACTIVATE_HOST",
                "HostProfile", String.valueOf(hostId), "active=" + active);
    }

    // =============================================
    // 4. BOOKING OPERATIONS & ISSUES (SELF-DRIVE ONLY)
    // =============================================

    public List<AdminBookingDto> getCitySelfDriveBookings(String adminEmail,
                                                          String city,
                                                          BookingStatus status,
                                                          String paymentStatus,
                                                          String search) {
        AppUser admin = authService.getAuthenticatedAdmin(adminEmail);
        List<String> upperCities = authService.getAuthorizedUpperCities(admin, city);

        return bookingRepository.findSelfDriveBookingsByCities(upperCities).stream()
                .filter(b -> status == null || b.getStatus() == status)
                .filter(b -> paymentStatus == null || paymentStatus.isBlank()
                        || derivePaymentStatus(b).equalsIgnoreCase(paymentStatus))
                .filter(b -> {
                    if (search == null || search.isBlank()) return true;
                    String q = search.trim().toLowerCase();
                    return String.valueOf(b.getId()).equals(q)
                            || b.getUser().getName().toLowerCase().contains(q)
                            || b.getCar().getCarNumber().toLowerCase().contains(q);
                })
                .map(b -> AdminBookingDto.builder()
                        .id(b.getId())
                        .customerName(b.getUser().getName())
                        .customerEmail(b.getUser().getEmail())
                        .customerContact(b.getUser().getEmail())
                        .vehicleName(b.getCar().getBrand() + " " + b.getCar().getModel())
                        .vehicleNumber(b.getCar().getCarNumber())
                        .carClass(b.getCar().getCarClass().name())
                        .hostName(b.getCar().getHost() != null ? b.getCar().getHost().getUser().getName() : "Platform Fleet")
                        .hostPhone(b.getCar().getHost() != null ? b.getCar().getHost().getPhone() : "-")
                        .startDate(b.getStartDate())
                        .endDate(b.getEndDate())
                        .pickupCity(b.getPickupCity())
                        .dropCity(b.getDropCity())
                        .pickupLocation(b.getPickupLocation())
                        .totalAmount(b.getGrossAmount() != null ? b.getGrossAmount() : BigDecimal.valueOf(b.getFinalPrice()))
                        .paymentStatus(derivePaymentStatus(b))
                        .bookingStatus(b.getStatus().name())
                        .bookingType(b.getBookingType().name())
                        .drivingLicenseNumber(b.getDrivingLicenseNumber())
                        .licenceDocumentUrl(b.getLicenceDocumentUrl())
                        .createdAt(b.getCreatedAt())
                        .build())
                .toList();
    }

    public AdminBookingDetailDto getBookingDetails(Long bookingId, String adminEmail) {
        AppUser admin = authService.getAuthenticatedAdmin(adminEmail);
        Booking b = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        authService.assertCityAuthorized(admin, b.getPickupCity());

        HostProfile host = b.getCar().getHost();
        List<AdminInspectionDto> inspections = inspectionRepository.findByBooking_IdOrderByCreatedAtDesc(bookingId)
                .stream().map(this::toInspectionDto).toList();
        List<AdminIssueDto> issues = incidentReportRepository.findByBooking_IdOrderByCreatedAtDesc(bookingId)
                .stream().map(this::toIssueDto).toList();

        return AdminBookingDetailDto.builder()
                .id(b.getId())
                .bookingType(b.getBookingType().name())
                .tripType(b.getTripType().name())
                .status(b.getStatus().name())
                .paymentStatus(derivePaymentStatus(b))
                .paymentRef(b.getPaymentRef())
                .paymentAt(b.getPaymentAt())
                .customerId(b.getUser().getId())
                .customerName(b.getUser().getName())
                .customerEmail(b.getUser().getEmail())
                .hostId(host != null ? host.getId() : null)
                .hostName(host != null ? host.getUser().getName() : "Legacy Fleet")
                .hostEmail(host != null ? host.getUser().getEmail() : null)
                .hostPhone(host != null ? host.getPhone() : null)
                .carId(b.getCar().getId())
                .vehicleName(b.getCar().getBrand() + " " + b.getCar().getModel())
                .vehicleNumber(b.getCar().getCarNumber())
                .carClass(b.getCar().getCarClass().name())
                .startDate(b.getStartDate())
                .endDate(b.getEndDate())
                .pickupCity(b.getPickupCity())
                .dropCity(b.getDropCity())
                .pickupLocation(b.getPickupLocation())
                .dropLocation(b.getDropLocation())
                .baseFare(b.getBaseFare())
                .basePrice(b.getBasePrice())
                .oneWayFee(b.getOneWayFee())
                .extraKmCharge(b.getExtraKmCharge())
                .finalPrice(b.getFinalPrice())
                .grossAmount(b.getGrossAmount())
                .platformCommissionPercent(b.getPlatformCommissionPercent())
                .platformCommissionAmount(b.getPlatformCommissionAmount())
                .platformFeeAmount(b.getPlatformFeeAmount())
                .hostEarningsAmount(b.getHostEarningsAmount())
                .drivingLicenseNumber(b.getDrivingLicenseNumber())
                .licenceDocumentUrl(b.getLicenceDocumentUrl())
                .licenceRemarks(b.getLicenceRemarks())
                .licenceSubmittedAt(b.getLicenceSubmittedAt())
                .licenceVerifiedAt(b.getLicenceVerifiedAt())
                .licenceVerifiedBy(b.getLicenceVerifiedBy())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .inspections(inspections)
                .relatedIssues(issues)
                .build();
    }

    @Transactional
    public Booking verifyLicense(Long bookingId, boolean approved, boolean resubmissionRequested,
                                 String remarks, String adminEmail) {
        AppUser admin = authService.getAuthenticatedAdmin(adminEmail);
        Booking b = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        authService.assertCityAuthorized(admin, b.getPickupCity());

        if (b.getStatus() != BookingStatus.VERIFICATION_PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking is not in verification pending status");
        }

        b.setLicenceVerifiedAt(Instant.now());
        b.setLicenceVerifiedBy(adminEmail + " (ID:" + admin.getId() + ")");
        if (remarks != null && !remarks.isBlank()) {
            b.setLicenceRemarks(remarks.trim());
        }

        if (approved) {
            b.setStatus(BookingStatus.CONFIRMED);
        } else if (resubmissionRequested) {
            b.setLicenceRemarks(remarks != null && !remarks.isBlank() ? remarks.trim() : "Licence resubmission requested");
        } else {
            b.setStatus(BookingStatus.REJECTED);
            Car car = b.getCar();
            car.setStatus(CarStatus.AVAILABLE);
            carRepository.save(car);
        }

        Booking saved = bookingRepository.save(b);
        auditLogService.log(
                approved ? "APPROVE_LICENCE" : (resubmissionRequested ? "RESUBMIT_LICENCE" : "REJECT_LICENCE"),
                "Booking", String.valueOf(bookingId),
                "Admin=" + adminEmail + (remarks != null ? " | Remarks: " + remarks : ""));
        return saved;
    }

    public List<AdminIssueDto> getCityIssues(String adminEmail, String city) {
        AppUser admin = authService.getAuthenticatedAdmin(adminEmail);
        List<String> upperCities = authService.getAuthorizedUpperCities(admin, city);
        return getCityIssuesInternal(upperCities).stream().map(this::toIssueDto).toList();
    }

    @Transactional
    public AdminIssueDto escalateIssue(Long issueId, String adminEmail, Long supportAgentId, String remarks) {
        AppUser admin = authService.getAuthenticatedAdmin(adminEmail);
        IncidentReport issue = incidentReportRepository.findById(issueId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Issue not found"));

        String issueCity = resolveIssueCity(issue);
        authService.assertCityAuthorized(admin, issueCity);

        if (supportAgentId != null) {
            AppUser agent = userRepository.findById(supportAgentId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Support Agent not found"));
            if (agent.getRole() != UserRole.SUPPORT_AGENT) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected user is not a Support Agent");
            }
            issue.setAssignedSupportAgent(agent);
        }

        issue.setStatus(IncidentStatus.IN_REVIEW);
        issue.setEscalatedAt(Instant.now());
        if (remarks != null && !remarks.isBlank()) {
            issue.setInternalRemarks(remarks.trim());
        }

        IncidentReport saved = incidentReportRepository.save(issue);
        auditLogService.log("ESCALATE_BOOKING_ISSUE", "IncidentReport", String.valueOf(issueId),
                "AssignedAgent=" + supportAgentId + " | Remarks: " + remarks);
        return toIssueDto(saved);
    }

    @Transactional
    public AdminIssueDto resolveIssue(Long issueId, String adminEmail, String resolutionDetails, String status) {
        AppUser admin = authService.getAuthenticatedAdmin(adminEmail);
        IncidentReport issue = incidentReportRepository.findById(issueId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Issue not found"));

        String issueCity = resolveIssueCity(issue);
        authService.assertCityAuthorized(admin, issueCity);

        IncidentStatus targetStatus = IncidentStatus.RESOLVED;
        if (status != null && !status.isBlank()) {
            targetStatus = IncidentStatus.valueOf(status.toUpperCase());
        }
        issue.setStatus(targetStatus);
        if (resolutionDetails != null && !resolutionDetails.isBlank()) {
            issue.setResolutionDetails(resolutionDetails.trim());
        }
        if (targetStatus == IncidentStatus.RESOLVED) {
            issue.setResolvedAt(Instant.now());
        }

        IncidentReport saved = incidentReportRepository.save(issue);
        auditLogService.log("RESOLVE_BOOKING_ISSUE", "IncidentReport", String.valueOf(issueId),
                "Status=" + targetStatus + " | Resolution: " + resolutionDetails);
        return toIssueDto(saved);
    }

    public List<StaffSummaryDto> getAvailableSupportAgents() {
        return userRepository.findByRoleAndIsActive(UserRole.SUPPORT_AGENT, true).stream()
                .map(u -> StaffSummaryDto.builder()
                        .id(u.getId())
                        .name(u.getName())
                        .email(u.getEmail())
                        .role(u.getRole())
                        .city(u.getCity())
                        .operationalScope(u.getOperationalScope())
                        .active(u.isActive())
                        .build())
                .toList();
    }

    // =============================================
    // 5. INSPECTION MANAGEMENT MODULE
    // =============================================

    public List<AdminInspectionDto> getCityInspections(String adminEmail,
                                                       String city,
                                                       InspectionStatus status,
                                                       InspectionType type) {
        AppUser admin = authService.getAuthenticatedAdmin(adminEmail);
        List<String> upperCities = authService.getAuthorizedUpperCities(admin, city);

        return inspectionRepository.findByCarCityIn(upperCities).stream()
                .filter(i -> status == null || i.getStatus() == status)
                .filter(i -> type == null || i.getInspectionType() == type)
                .map(this::toInspectionDto)
                .toList();
    }

    public AdminInspectionDto getInspectionDetails(Long inspectionId, String adminEmail) {
        AppUser admin = authService.getAuthenticatedAdmin(adminEmail);
        VehicleInspection insp = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inspection not found"));
        authService.assertCityAuthorized(admin, insp.getCar().getCurrentCity());
        return toInspectionDto(insp);
    }

    @Transactional
    public AdminInspectionDto escalateInspection(Long inspectionId, String adminEmail, String remarks, boolean suspendVehicle) {
        if (remarks == null || remarks.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Escalation remarks are required");
        }
        AppUser admin = authService.getAuthenticatedAdmin(adminEmail);
        VehicleInspection insp = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inspection not found"));
        authService.assertCityAuthorized(admin, insp.getCar().getCurrentCity());

        insp.setStatus(InspectionStatus.ESCALATED);
        insp.setEscalatedByAdmin(adminEmail);
        insp.setEscalationRemarks(remarks.trim());
        VehicleInspection saved = inspectionRepository.save(insp);

        if (suspendVehicle && insp.getCar().getStatus() != CarStatus.BOOKED) {
            Car car = insp.getCar();
            car.setStatus(CarStatus.SUSPENDED);
            car.setActive(false);
            car.setAdminRemarks("Suspended from inspection escalation: " + remarks.trim());
            carRepository.save(car);
            recordListingHistory(car, "SUSPENDED_FROM_INSPECTION", adminEmail, remarks.trim());
        }

        auditLogService.log("ESCALATE_INSPECTION", "VehicleInspection", String.valueOf(inspectionId),
                "SuspendVehicle=" + suspendVehicle + " | Remarks: " + remarks.trim());
        return toInspectionDto(saved);
    }

    // =============================================
    // HELPERS
    // =============================================

    private AdminCarListingDto toCarListingDto(Car c) {
        CarPricing pricing = carPricingRepository.findFirstByCarClassAndActiveTrue(c.getCarClass()).orElse(null);
        String violationMsg = checkPricingViolation(c, pricing);
        HostProfile host = c.getHost();
        return AdminCarListingDto.builder()
                .id(c.getId())
                .carNumber(c.getCarNumber())
                .brand(c.getBrand())
                .model(c.getModel())
                .carClass(c.getCarClass().name())
                .fuelType(c.getFuelType().name())
                .transmission(c.getTransmission().name())
                .pricePerDay(c.getPricePerDay())
                .currentCity(c.getCurrentCity())
                .currentLocation(c.getCurrentLocation())
                .approvalStatus(c.getApprovalStatus() != null ? c.getApprovalStatus().name() : "APPROVED")
                .status(c.getStatus().name())
                .active(c.isActive())
                .pricingOutOfRange(violationMsg != null || Boolean.TRUE.equals(c.getPricingOutOfRange()))
                .pricingViolationMessage(violationMsg)
                .hostId(host != null ? host.getId() : null)
                .hostName(host != null ? host.getUser().getName() : "Legacy Fleet")
                .hostPhone(host != null ? host.getPhone() : "-")
                .hostEmail(host != null ? host.getUser().getEmail() : null)
                .submittedAt(c.getSubmittedAt())
                .build();
    }

    private String checkPricingViolation(Car c, CarPricing pricing) {
        if (pricing == null) return null;
        if (c.getPricePerDay() < pricing.getMinPerDayRate()) {
            return "Below minimum permitted price (₹" + pricing.getMinPerDayRate() + ")";
        }
        if (c.getPricePerDay() > pricing.getMaxPerDayRate()) {
            return "Exceeds maximum permitted price (₹" + pricing.getMaxPerDayRate() + ")";
        }
        return null;
    }

    private void recordListingHistory(Car car, String action, String actorEmail, String remarks) {
        listingHistoryRepository.save(ListingHistory.builder()
                .car(car)
                .action(action)
                .actorEmail(actorEmail)
                .remarks(remarks)
                .build());
    }

    private String maskIdNumber(String idNumber) {
        if (idNumber == null || idNumber.length() <= 4) return idNumber;
        return "*".repeat(idNumber.length() - 4) + idNumber.substring(idNumber.length() - 4);
    }

    private String derivePaymentStatus(Booking b) {
        if (b.getStatus() == BookingStatus.PENDING_PAYMENT) return "PENDING";
        if (b.getStatus() == BookingStatus.CANCELLED || b.getStatus() == BookingStatus.REJECTED) {
            return b.getPaymentRef() != null ? "REFUNDED" : "FAILED";
        }
        return b.getPaymentRef() != null ? "SUCCESS" : "PENDING";
    }

    private List<IncidentReport> getCityIssuesInternal(List<String> upperCities) {
        return incidentReportRepository.findAllWithDetails().stream()
                .filter(i -> {
                    String c = resolveIssueCity(i);
                    return c != null && upperCities.contains(c.trim().toUpperCase());
                })
                .toList();
    }

    private String resolveIssueCity(IncidentReport i) {
        if (i.getCity() != null && !i.getCity().isBlank()) return i.getCity();
        if (i.getBooking() != null) return i.getBooking().getPickupCity();
        if (i.getReportedUser() != null) return i.getReportedUser().getCity();
        return null;
    }

    private AdminIssueDto toIssueDto(IncidentReport i) {
        return AdminIssueDto.builder()
                .id(i.getId())
                .bookingId(i.getBooking() != null ? i.getBooking().getId() : null)
                .issueType(i.getIssueType() != null ? i.getIssueType() : "GENERAL")
                .title(i.getTitle())
                .description(i.getDescription())
                .status(i.getStatus().name())
                .city(resolveIssueCity(i))
                .reportedByName(i.getReportedBy().getName())
                .reportedByEmail(i.getReportedBy().getEmail())
                .reportedUserName(i.getReportedUser().getName())
                .assignedSupportAgentId(i.getAssignedSupportAgent() != null ? i.getAssignedSupportAgent().getId() : null)
                .assignedSupportAgentName(i.getAssignedSupportAgent() != null ? i.getAssignedSupportAgent().getName() : null)
                .internalRemarks(i.getInternalRemarks())
                .resolutionDetails(i.getResolutionDetails())
                .escalatedAt(i.getEscalatedAt())
                .resolvedAt(i.getResolvedAt())
                .createdAt(i.getCreatedAt())
                .build();
    }

    private AdminInspectionDto toInspectionDto(VehicleInspection v) {
        List<String> photos = (v.getPhotoUrls() != null && !v.getPhotoUrls().isBlank())
                ? Arrays.stream(v.getPhotoUrls().split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList()
                : Collections.emptyList();
        return AdminInspectionDto.builder()
                .id(v.getId())
                .carId(v.getCar().getId())
                .carNumber(v.getCar().getCarNumber())
                .vehicleName(v.getCar().getBrand() + " " + v.getCar().getModel())
                .city(v.getCar().getCurrentCity())
                .hostName(v.getCar().getHost() != null ? v.getCar().getHost().getUser().getName() : "Legacy Fleet")
                .bookingId(v.getBooking() != null ? v.getBooking().getId() : null)
                .inspectionType(v.getInspectionType().name())
                .fleetManagerId(v.getFleetManager() != null ? v.getFleetManager().getId() : null)
                .fleetManagerName(v.getFleetManager() != null ? v.getFleetManager().getName() : "Unassigned")
                .status(v.getStatus().name())
                .result(v.getResult())
                .damageDetails(v.getDamageDetails())
                .photoUrls(photos)
                .fleetManagerRemarks(v.getFleetManagerRemarks())
                .escalatedByAdmin(v.getEscalatedByAdmin())
                .escalationRemarks(v.getEscalationRemarks())
                .scheduledDate(v.getScheduledDate())
                .completedDate(v.getCompletedDate())
                .createdAt(v.getCreatedAt())
                .build();
    }
}
