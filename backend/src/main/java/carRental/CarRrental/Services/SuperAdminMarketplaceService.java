package carRental.CarRrental.Services;

import carRental.CarRrental.Dtos.*;
import carRental.CarRrental.Models.*;
import carRental.CarRrental.Repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SuperAdminMarketplaceService {

    @Value("${app.frontend.url}")
    private String frontendUrl;

    private final AppUserRepository appUserRepository;
    private final HostProfileRepository hostProfileRepository;
    private final CarRepository carRepository;
    private final BookingRepository bookingRepository;
    private final HostPayoutRepository hostPayoutRepository;
    private final ReviewRepository reviewRepository;
    private final IncidentReportRepository incidentReportRepository;
    private final ServiceCityRepository serviceCityRepository;
    private final PlatformSettingsRepository platformSettingsRepository;
    private final HostStatusHistoryRepository hostStatusHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final EmailService emailService;
    private final AuditLogService auditLogService;

    // =============================================
    // 1. ENHANCED USER MANAGEMENT
    // =============================================

    public List<UserSummaryDto> getAllUserSummaries() {
        return appUserRepository.findByRole(UserRole.USER).stream()
                .map(u -> UserSummaryDto.builder()
                        .id(u.getId())
                        .name(u.getName())
                        .email(u.getEmail())
                        .active(u.isActive())
                        .suspended(u.isSuspendedFlag())
                        .suspensionReason(u.getSuspensionReason())
                        .emailVerified(u.isEmailVerified())
                        .createdAt(u.getCreatedAt())
                        .build())
                .toList();
    }

    public UserDetailDto getUserDetails(Long userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getRole() != UserRole.USER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account is not a customer user");
        }

        List<Booking> bookings = bookingRepository.findByUserOrderByCreatedAtDesc(user);

        List<UserDetailDto.BookingSummaryItem> bookingHistory = bookings.stream()
                .map(b -> UserDetailDto.BookingSummaryItem.builder()
                        .id(b.getId())
                        .carName(b.getCar() != null ? b.getCar().getBrand() + " " + b.getCar().getModel() : "N/A")
                        .carNumber(b.getCar() != null ? b.getCar().getCarNumber() : "N/A")
                        .status(b.getStatus().name())
                        .startDate(b.getStartDate())
                        .endDate(b.getEndDate())
                        .pickupCity(b.getPickupCity())
                        .dropCity(b.getDropCity())
                        .amount(b.getGrossAmount() != null ? b.getGrossAmount() : BigDecimal.valueOf(b.getFinalPrice()))
                        .createdAt(b.getCreatedAt())
                        .build())
                .toList();

        List<UserDetailDto.PaymentHistoryItem> paymentHistory = bookings.stream()
                .filter(b -> b.getPaymentRef() != null)
                .map(b -> UserDetailDto.PaymentHistoryItem.builder()
                        .bookingId(b.getId())
                        .paymentRef(b.getPaymentRef())
                        .amount(b.getGrossAmount() != null ? b.getGrossAmount() : BigDecimal.valueOf(b.getFinalPrice()))
                        .status(b.getStatus().name())
                        .paymentAt(b.getPaymentAt())
                        .build())
                .toList();

        List<UserDetailDto.ReviewItem> reviews = reviewRepository.findByTargetUserIdWithReviewer(userId).stream()
                .map(r -> UserDetailDto.ReviewItem.builder()
                        .id(r.getId())
                        .reviewerName(r.getReviewer().getName())
                        .rating(r.getRating())
                        .comment(r.getComment())
                        .createdAt(r.getCreatedAt())
                        .build())
                .toList();

        List<UserDetailDto.IncidentItem> incidents = incidentReportRepository.findByReportedUserIdWithReporter(userId).stream()
                .map(i -> UserDetailDto.IncidentItem.builder()
                        .id(i.getId())
                        .title(i.getTitle())
                        .description(i.getDescription())
                        .status(i.getStatus().name())
                        .reportedByName(i.getReportedBy().getName())
                        .createdAt(i.getCreatedAt())
                        .build())
                .toList();

        return UserDetailDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .active(user.isActive())
                .suspended(user.isSuspendedFlag())
                .suspensionReason(user.getSuspensionReason())
                .emailVerified(user.isEmailVerified())
                .createdAt(user.getCreatedAt())
                .bookingHistory(bookingHistory)
                .paymentHistory(paymentHistory)
                .reviews(reviews)
                .reportedIncidents(incidents)
                .build();
    }

    @Transactional
    public void suspendUser(Long userId, String reason) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.getRole() != UserRole.USER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account is not a USER");
        }
        user.setSuspended(true);
        user.setSuspensionReason(reason != null && !reason.isBlank() ? reason.trim() : "Suspended by Super Admin");
        user.setActive(false);
        appUserRepository.save(user);
        auditLogService.log("SUSPEND_USER", "AppUser", String.valueOf(userId), "Reason: " + user.getSuspensionReason());
    }

    @Transactional
    public void unsuspendUser(Long userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.getRole() != UserRole.USER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account is not a USER");
        }
        user.setSuspended(false);
        user.setSuspensionReason(null);
        user.setActive(true);
        appUserRepository.save(user);
        auditLogService.log("UNSUSPEND_USER", "AppUser", String.valueOf(userId), "User account reactivated");
    }

    // =============================================
    // 2. HOST MANAGEMENT (REPLACES DRIVER MANAGEMENT)
    // =============================================

    public List<HostSummaryDto> getAllHosts(String search,
                                            HostVerificationStatus verificationStatus,
                                            Boolean active) {
        return hostProfileRepository.findAllWithUser().stream()
                .filter(h -> {
                    if (verificationStatus != null && h.getVerificationStatus() != verificationStatus) {
                        return false;
                    }
                    if (active != null && h.isActive() != active) {
                        return false;
                    }
                    if (search != null && !search.isBlank()) {
                        String q = search.trim().toLowerCase();
                        boolean matchName = h.getUser().getName() != null && h.getUser().getName().toLowerCase().contains(q);
                        boolean matchEmail = h.getUser().getEmail() != null && h.getUser().getEmail().toLowerCase().contains(q);
                        boolean matchCity = h.getCity() != null && h.getCity().toLowerCase().contains(q);
                        boolean matchBiz = h.getBusinessName() != null && h.getBusinessName().toLowerCase().contains(q);
                        return matchName || matchEmail || matchCity || matchBiz;
                    }
                    return true;
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

    public HostDetailDto getHostDetails(Long hostId) {
        HostProfile host = hostProfileRepository.findById(hostId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Host not found"));
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
                .governmentIdNumber(host.getGovernmentIdNumber())
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
    public HostSummaryDto verifyHost(Long hostId, HostVerificationRequest req) {
        HostProfile host = hostProfileRepository.findById(hostId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Host not found"));

        String actorEmail = getCurrentActorEmail();
        if (host.getUser().getEmail().equalsIgnoreCase(actorEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "A Host cannot approve their own verification");
        }

        if ((req.getStatus() == HostVerificationStatus.REJECTED
                || req.getStatus() == HostVerificationStatus.RESUBMISSION_REQUIRED)
                && (req.getReason() == null || req.getReason().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reason is required when rejecting or requesting resubmission");
        }

        HostVerificationStatus previousStatus = host.getVerificationStatus();
        boolean approved = req.getStatus() == HostVerificationStatus.VERIFIED
                || req.getStatus() == HostVerificationStatus.APPROVED;
        host.setVerificationStatus(req.getStatus());
        host.setRejectionReason(approved ? null : req.getReason());
        host.setVerifiedBy(actorEmail);
        host.setVerifiedAt(Instant.now());
        if (approved) {
            host.setActive(true);
            AppUser hostUser = host.getUser();
            hostUser.grantRole(UserRole.HOST);
            hostUser.incrementTokenVersion();
            appUserRepository.save(hostUser);
        } else if (req.getStatus() == HostVerificationStatus.REJECTED) {
            host.setActive(false);
        }

        HostProfile saved = hostProfileRepository.save(host);
        hostStatusHistoryRepository.save(HostStatusHistory.builder()
                .hostProfile(saved)
                .previousStatus(previousStatus)
                .newStatus(req.getStatus())
                .changedBy(actorEmail)
                .remarks(approved ? "Approved by Super Admin" : req.getReason())
                .build());

        emailService.sendEmail(
                saved.getUser().getEmail(),
                approved ? "Host Application Approved - Car Rental" : "Host Application Update - Car Rental",
                approved
                        ? "Hello " + saved.getUser().getName() + ",\n\nYour Host application has been approved. Please log in again or refresh your session to access the Host Portal.\n\nBest regards,\nCar Rental Team"
                        : "Hello " + saved.getUser().getName() + ",\n\nYour Host application status is now " + req.getStatus().name()
                        + (req.getReason() != null ? ".\nReason: " + req.getReason() : "") + "\n\nBest regards,\nCar Rental Team"
        );
        auditLogService.log("VERIFY_HOST_" + req.getStatus().name(), "HostProfile", String.valueOf(hostId),
                "Status: " + req.getStatus() + (req.getReason() != null ? " | Reason: " + req.getReason() : ""));

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
    public void setHostActiveStatus(Long hostId, boolean active) {
        HostProfile host = hostProfileRepository.findById(hostId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Host not found"));
        host.setActive(active);
        host.getUser().setActive(active);
        appUserRepository.save(host.getUser());
        hostProfileRepository.save(host);
        auditLogService.log(active ? "REACTIVATE_HOST" : "DEACTIVATE_HOST", "HostProfile", String.valueOf(hostId),
                "Host active=" + active);
    }

    @Transactional
    public void suspendHost(Long hostId, String reason) {
        HostProfile host = hostProfileRepository.findById(hostId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Host not found"));
        host.setSuspended(true);
        host.setSuspensionReason(reason != null && !reason.isBlank() ? reason.trim() : "Suspended by Super Admin");
        host.setActive(false);
        host.getUser().setSuspended(true);
        host.getUser().setSuspensionReason(host.getSuspensionReason());
        host.getUser().setActive(false);
        appUserRepository.save(host.getUser());
        hostProfileRepository.save(host);
        auditLogService.log("SUSPEND_HOST", "HostProfile", String.valueOf(hostId), "Reason: " + host.getSuspensionReason());
    }

    @Transactional
    public void unsuspendHost(Long hostId) {
        HostProfile host = hostProfileRepository.findById(hostId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Host not found"));
        host.setSuspended(false);
        host.setSuspensionReason(null);
        host.setActive(true);
        host.getUser().setSuspended(false);
        host.getUser().setSuspensionReason(null);
        host.getUser().setActive(true);
        appUserRepository.save(host.getUser());
        hostProfileRepository.save(host);
        auditLogService.log("UNSUSPEND_HOST", "HostProfile", String.valueOf(hostId), "Host unsuspended");
    }

    // =============================================
    // 3. ENHANCED CITY MANAGEMENT
    // =============================================

    public List<ServiceCityResponseDto> getAllCitiesWithMetrics() {
        return serviceCityRepository.findAll().stream()
                .map(c -> {
                    AdminSummaryDto adminDto = null;
                    if (c.getAdmin() != null) {
                        AppUser a = c.getAdmin();
                        adminDto = AdminSummaryDto.builder()
                                .id(a.getId())
                                .name(a.getName())
                                .email(a.getEmail())
                                .role(a.getRole())
                                .city(a.getCity())
                                .active(a.isActive())
                                .emailVerified(a.isEmailVerified())
                                .createdAt(a.getCreatedAt())
                                .build();
                    }
                    return ServiceCityResponseDto.builder()
                            .id(c.getId())
                            .cityName(c.getCityName())
                            .parkingAddress(c.getParkingAddress())
                            .parkingContact(c.getParkingContact())
                            .oneWayFee(c.getOneWayFee())
                            .localServiceCharge(c.getLocalServiceCharge() != null ? c.getLocalServiceCharge() : BigDecimal.ZERO)
                            .active(c.isActive())
                            .bookingAvailable(!Boolean.FALSE.equals(c.getBookingAvailable()))
                            .admin(adminDto)
                            .hostCount(hostProfileRepository.countByCityIgnoreCase(c.getCityName()))
                            .vehicleCount(carRepository.countByCurrentCityIgnoreCaseAndActiveTrue(c.getCityName()))
                            .activeBookingCount(bookingRepository.countActiveBookingsByCity(c.getCityName()))
                            .build();
                })
                .toList();
    }

    @Transactional
    public ServiceCity updateCity(Long id, ServiceCityRequest req) {
        ServiceCity city = serviceCityRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "City not found"));

        if (req.getCityName() != null && !req.getCityName().isBlank()
                && !city.getCityName().equalsIgnoreCase(req.getCityName().trim())) {
            serviceCityRepository.findByCityNameIgnoreCase(req.getCityName().trim())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(id)) {
                            throw new ResponseStatusException(HttpStatus.CONFLICT, "City already exists: " + req.getCityName());
                        }
                    });
            city.setCityName(req.getCityName().trim());
        }

        if (req.getParkingAddress() != null && !req.getParkingAddress().isBlank()) {
            city.setParkingAddress(req.getParkingAddress().trim());
        }
        if (req.getParkingContact() != null && !req.getParkingContact().isBlank()) {
            city.setParkingContact(req.getParkingContact().trim());
        }
        if (req.getOneWayFee() != null) {
            city.setOneWayFee(req.getOneWayFee());
        }
        if (req.getLocalServiceCharge() != null) {
            city.setLocalServiceCharge(req.getLocalServiceCharge());
        }
        if (req.getBookingAvailable() != null) {
            city.setBookingAvailable(req.getBookingAvailable());
        }
        if (req.getActive() != null) {
            city.setActive(req.getActive());
        }

        if (req.getAdminId() != null) {
            AppUser admin = appUserRepository.findById(req.getAdminId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found"));
            if (admin.getRole() != UserRole.ADMIN) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not an admin");
            }
            if (admin.getCity() == null || !admin.getCity().equalsIgnoreCase(city.getCityName())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Admin's city (" + admin.getCity() + ") does not match city (" + city.getCityName() + ")");
            }
            city.setAdmin(admin);
        }

        ServiceCity saved = serviceCityRepository.save(city);
        auditLogService.log("UPDATE_CITY", "ServiceCity", String.valueOf(id), "Updated city: " + saved.getCityName());
        return saved;
    }

    @Transactional
    public void deleteCity(Long id) {
        ServiceCity city = serviceCityRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "City not found"));

        long vehicles = carRepository.countByCurrentCityIgnoreCaseAndActiveTrue(city.getCityName());
        long bookings = bookingRepository.countByPickupCityIgnoreCaseOrDropCityIgnoreCase(city.getCityName(), city.getCityName());
        if (vehicles > 0 || bookings > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot delete city referenced by vehicles or bookings. Deactivate it instead.");
        }

        serviceCityRepository.delete(city);
        auditLogService.log("DELETE_CITY", "ServiceCity", String.valueOf(id), "Deleted city: " + city.getCityName());
    }

    // =============================================
    // 4. FLEET MANAGERS & SUPPORT AGENTS
    // =============================================

    public List<StaffSummaryDto> getStaffByRole(UserRole role) {
        return appUserRepository.findByRole(role).stream()
                .map(u -> StaffSummaryDto.builder()
                        .id(u.getId())
                        .name(u.getName())
                        .email(u.getEmail())
                        .role(u.getRole())
                        .city(u.getCity())
                        .operationalScope(u.getOperationalScope())
                        .active(u.isActive())
                        .emailVerified(u.isEmailVerified())
                        .createdAt(u.getCreatedAt())
                        .build())
                .toList();
    }

    @Transactional
    public StaffSummaryDto createStaffAccount(StaffAccountRequest req, UserRole role) {
        if (role != UserRole.FLEET_MANAGER && role != UserRole.SUPPORT_AGENT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid staff role");
        }

        String email = req.getEmail().trim().toLowerCase();
        if (appUserRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists: " + email);
        }

        String rawPassword = (req.getPassword() != null && !req.getPassword().isBlank())
                ? req.getPassword()
                : UUID.randomUUID().toString();

        AppUser user = AppUser.builder()
                .name(req.getName().trim())
                .email(email)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .role(role)
                .emailVerified(false)
                .isActive(true)
                .city(req.getCity() != null ? req.getCity().trim() : null)
                .operationalScope(req.getOperationalScope() != null ? req.getOperationalScope().trim() : null)
                .build();

        AppUser saved = appUserRepository.save(user);

        TokenService.GeneratedToken setupToken = tokenService.createAccountSetupToken(saved);
        String link = frontendUrl + "/setup-account?token=" + setupToken.rawToken();
        emailService.sendEmail(
                saved.getEmail(),
                "Set Up Your " + (role == UserRole.FLEET_MANAGER ? "Fleet Manager" : "Support Agent") + " Account",
                "Hello " + saved.getName() + ",\n\n" +
                        "You have been added as a " + role.name().replace('_', ' ') + " in the Car Rental Marketplace.\n" +
                        "Please click the link below to set up your password and activate your account:\n" +
                        link + "\n\nBest regards,\nCar Rental Team"
        );

        auditLogService.log("CREATE_" + role.name(), "AppUser", String.valueOf(saved.getId()),
                "Created " + role + " account for " + saved.getEmail());

        return StaffSummaryDto.builder()
                .id(saved.getId())
                .name(saved.getName())
                .email(saved.getEmail())
                .role(saved.getRole())
                .city(saved.getCity())
                .operationalScope(saved.getOperationalScope())
                .active(saved.isActive())
                .emailVerified(saved.isEmailVerified())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Transactional
    public StaffSummaryDto assignStaffScope(Long userId, UserRole expectedRole, String city, String operationalScope) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        if (user.getRole() != expectedRole) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account is not a " + expectedRole);
        }
        if (city != null) {
            user.setCity(city.trim().isEmpty() ? null : city.trim());
        }
        if (operationalScope != null) {
            user.setOperationalScope(operationalScope.trim().isEmpty() ? null : operationalScope.trim());
        }
        AppUser saved = appUserRepository.save(user);
        auditLogService.log("ASSIGN_SCOPE_" + expectedRole.name(), "AppUser", String.valueOf(userId),
                "City=" + saved.getCity() + ", Scope=" + saved.getOperationalScope());

        return StaffSummaryDto.builder()
                .id(saved.getId())
                .name(saved.getName())
                .email(saved.getEmail())
                .role(saved.getRole())
                .city(saved.getCity())
                .operationalScope(saved.getOperationalScope())
                .active(saved.isActive())
                .emailVerified(saved.isEmailVerified())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Transactional
    public void setStaffActiveStatus(Long userId, UserRole expectedRole, boolean active) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        if (user.getRole() != expectedRole) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account is not a " + expectedRole);
        }
        user.setActive(active);
        appUserRepository.save(user);
        auditLogService.log((active ? "REACTIVATE_" : "DEACTIVATE_") + expectedRole.name(), "AppUser",
                String.valueOf(userId), "active=" + active);
    }

    // =============================================
    // 5. PLATFORM SETTINGS & AUDIT LOGS
    // =============================================

    public PlatformSettingsDto getPlatformSettings() {
        PlatformSettings settings = platformSettingsRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> platformSettingsRepository.save(PlatformSettings.builder().build()));
        return toSettingsDto(settings);
    }

    @Transactional
    public PlatformSettingsDto updatePlatformSettings(PlatformSettingsDto dto) {
        if (dto.getMinBookingDurationDays() > dto.getMaxBookingDurationDays()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Minimum booking duration cannot be greater than maximum booking duration");
        }

        PlatformSettings settings = platformSettingsRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> PlatformSettings.builder().build());

        settings.setDefaultPlatformCommissionPercent(dto.getDefaultPlatformCommissionPercent());
        settings.setDefaultPlatformServiceFee(dto.getDefaultPlatformServiceFee());
        settings.setMinBookingDurationDays(dto.getMinBookingDurationDays());
        settings.setMaxBookingDurationDays(dto.getMaxBookingDurationDays());
        settings.setCancellationPolicy(dto.getCancellationPolicy().trim());
        settings.setRefundPolicy(dto.getRefundPolicy().trim());
        settings.setHostVerificationRequired(dto.isHostVerificationRequired());
        settings.setGuestVerificationRequired(dto.isGuestVerificationRequired());
        settings.setVehicleListingApprovalRequired(dto.isVehicleListingApprovalRequired());
        settings.setOneWayBookingEnabled(dto.isOneWayBookingEnabled());
        settings.setUpdatedBy(getCurrentActorEmail());

        PlatformSettings saved = platformSettingsRepository.save(settings);
        auditLogService.log("UPDATE_PLATFORM_SETTINGS", "PlatformSettings", String.valueOf(saved.getId()),
                "Commission=" + saved.getDefaultPlatformCommissionPercent() + "%, ServiceFee=" + saved.getDefaultPlatformServiceFee());

        return toSettingsDto(saved);
    }

    public Page<AuditLog> getAuditLogs(Pageable pageable) {
        return auditLogService.getRecentLogs(pageable);
    }

    private PlatformSettingsDto toSettingsDto(PlatformSettings s) {
        return PlatformSettingsDto.builder()
                .defaultPlatformCommissionPercent(s.getDefaultPlatformCommissionPercent())
                .defaultPlatformServiceFee(s.getDefaultPlatformServiceFee())
                .minBookingDurationDays(s.getMinBookingDurationDays())
                .maxBookingDurationDays(s.getMaxBookingDurationDays())
                .cancellationPolicy(s.getCancellationPolicy())
                .refundPolicy(s.getRefundPolicy())
                .hostVerificationRequired(s.isHostVerificationRequired())
                .guestVerificationRequired(s.isGuestVerificationRequired())
                .vehicleListingApprovalRequired(s.isVehicleListingApprovalRequired())
                .oneWayBookingEnabled(s.isOneWayBookingEnabled())
                .updatedAt(s.getUpdatedAt())
                .updatedBy(s.getUpdatedBy())
                .build();
    }

    private String getCurrentActorEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "SYSTEM";
    }
}
