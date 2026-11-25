package carRental.CarRrental.Services;

import carRental.CarRrental.Models.*;
import carRental.CarRrental.Repositories.AppUserRepository;
import carRental.CarRrental.Repositories.HostProfileRepository;
import carRental.CarRrental.Repositories.HostStatusHistoryRepository;
import carRental.CarRrental.Repositories.ServiceCityRepository;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HostOnboardingService {

    private final AppUserRepository userRepository;
    private final HostProfileRepository hostProfileRepository;
    private final HostStatusHistoryRepository historyRepository;
    private final ServiceCityRepository serviceCityRepository;
    private final DocumentStorageService documentStorageService;
    private final AdminAuthorizationService adminAuthorizationService;
    private final EmailService emailService;
    private final AuditLogService auditLogService;

    public HostOnboardingService(
            AppUserRepository userRepository,
            HostProfileRepository hostProfileRepository,
            HostStatusHistoryRepository historyRepository,
            ServiceCityRepository serviceCityRepository,
            DocumentStorageService documentStorageService,
            AdminAuthorizationService adminAuthorizationService,
            EmailService emailService,
            AuditLogService auditLogService
    ) {
        this.userRepository = userRepository;
        this.hostProfileRepository = hostProfileRepository;
        this.historyRepository = historyRepository;
        this.serviceCityRepository = serviceCityRepository;
        this.documentStorageService = documentStorageService;
        this.adminAuthorizationService = adminAuthorizationService;
        this.emailService = emailService;
        this.auditLogService = auditLogService;
    }

    public Map<String, Object> getOnboardingStatus(String email) {
        AppUser user = getUserByEmail(email);
        HostProfile profile = hostProfileRepository.findByUser_Id(user.getId()).orElse(null);

        List<String> availableCities = serviceCityRepository.findByActiveTrue().stream()
                .map(ServiceCity::getCityName)
                .sorted()
                .toList();

        List<Map<String, Object>> history = profile == null
                ? List.of()
                : historyRepository.findByHostProfile_IdOrderByCreatedAtDesc(profile.getId()).stream()
                .map(h -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("previousStatus", h.getPreviousStatus() != null ? h.getPreviousStatus().name() : null);
                    item.put("newStatus", h.getNewStatus().name());
                    item.put("changedBy", h.getChangedBy());
                    item.put("remarks", h.getRemarks());
                    item.put("createdAt", h.getCreatedAt());
                    return item;
                })
                .toList();

        Map<String, Object> res = new HashMap<>();
        res.put("userId", user.getId());
        res.put("name", user.getName());
        res.put("email", user.getEmail());
        res.put("mobileNumber", user.getMobileNumber());
        res.put("emailVerified", user.isEmailVerified());
        res.put("mobileVerified", Boolean.TRUE.equals(user.getMobileVerified()));
        res.put("roles", user.getEffectiveRoles().stream().map(Enum::name).toList());
        res.put("availableCities", availableCities);
        res.put("history", history);

        if (profile == null) {
            res.put("hasApplication", false);
            res.put("status", "NOT_STARTED");
            return res;
        }

        res.put("hasApplication", true);
        res.put("hostProfileId", profile.getId());
        res.put("status", profile.getVerificationStatus().name());
        res.put("fullName", user.getName());
        res.put("businessName", profile.getBusinessName());
        res.put("phone", profile.getPhone());
        res.put("city", profile.getCity());
        res.put("address", profile.getAddress());
        res.put("state", profile.getState());
        res.put("postalCode", profile.getPostalCode());
        res.put("governmentIdNumber", maskIdentifier(profile.getGovernmentIdNumber()));
        res.put("hasGovernmentId", profile.getGovernmentIdNumber() != null && !profile.getGovernmentIdNumber().isBlank());
        res.put("documentType", profile.getDocumentType());
        res.put("documentUrl", profile.getDocumentUrl());
        res.put("panNumber", maskIdentifier(profile.getPanNumber()));
        res.put("profilePhotoUrl", profile.getProfilePhotoUrl());
        res.put("description", profile.getDescription());
        res.put("termsAccepted", Boolean.TRUE.equals(profile.getTermsAccepted()));
        res.put("rejectionReason", profile.getRejectionReason());
        res.put("submittedAt", profile.getSubmittedAt());
        res.put("verifiedAt", profile.getVerifiedAt());
        res.put("verifiedBy", profile.getVerifiedBy());
        return res;
    }

    @Transactional
    public Map<String, Object> saveDraft(String email, Map<String, Object> payload) {
        AppUser user = getUserByEmail(email);
        if (!user.isEmailVerified()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Please verify your email before starting Host onboarding.");
        }

        HostProfile profile = hostProfileRepository.findByUser_Id(user.getId())
                .orElseGet(() -> HostProfile.builder()
                        .user(user)
                        .phone(user.getMobileNumber() != null ? user.getMobileNumber() : "")
                        .city("PENDING")
                        .verificationStatus(HostVerificationStatus.DRAFT)
                        .active(false)
                        .build());

        HostVerificationStatus currentStatus = profile.getVerificationStatus();
        if (currentStatus == HostVerificationStatus.VERIFIED
                || currentStatus == HostVerificationStatus.APPROVED
                || currentStatus == HostVerificationStatus.SUSPENDED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Host application cannot be modified in its current status.");
        }

        if (payload.containsKey("fullName") && payload.get("fullName") != null) {
            String updatedName = String.valueOf(payload.get("fullName")).trim();
            if (!updatedName.isBlank()) {
                user.setName(updatedName);
                userRepository.save(user);
            }
        }
        if (payload.containsKey("businessName") && payload.get("businessName") != null) {
            profile.setBusinessName(String.valueOf(payload.get("businessName")).trim());
        }
        if (payload.containsKey("phone") && payload.get("phone") != null) {
            String phone = String.valueOf(payload.get("phone")).trim();
            profile.setPhone(phone);
            if (!phone.isBlank() && !phone.equals(user.getMobileNumber())) {
                user.setMobileNumber(phone);
                user.setMobileVerified(false);
                userRepository.save(user);
            }
        }
        if (payload.containsKey("city") && payload.get("city") != null) {
            profile.setCity(String.valueOf(payload.get("city")).trim());
        }
        if (payload.containsKey("address") && payload.get("address") != null) {
            profile.setAddress(String.valueOf(payload.get("address")).trim());
        }
        if (payload.containsKey("state") && payload.get("state") != null) {
            profile.setState(String.valueOf(payload.get("state")).trim());
        }
        if (payload.containsKey("postalCode") && payload.get("postalCode") != null) {
            profile.setPostalCode(String.valueOf(payload.get("postalCode")).trim());
        }
        if (payload.containsKey("governmentIdNumber") && payload.get("governmentIdNumber") != null) {
            String govId = String.valueOf(payload.get("governmentIdNumber")).trim();
            if (!govId.isBlank() && !govId.contains("*")) {
                profile.setGovernmentIdNumber(govId);
            }
        }
        if (payload.containsKey("documentType") && payload.get("documentType") != null) {
            profile.setDocumentType(String.valueOf(payload.get("documentType")).trim());
        }
        if (payload.containsKey("panNumber") && payload.get("panNumber") != null) {
            String pan = String.valueOf(payload.get("panNumber")).trim().toUpperCase();
            if (!pan.isBlank() && !pan.contains("*")) {
                profile.setPanNumber(pan);
            }
        }
        if (payload.containsKey("description") && payload.get("description") != null) {
            profile.setDescription(String.valueOf(payload.get("description")).trim());
        }
        if (payload.containsKey("termsAccepted") && payload.get("termsAccepted") instanceof Boolean accepted) {
            profile.setTermsAccepted(accepted);
            user.setTermsAccepted(accepted);
            userRepository.save(user);
        }

        if (currentStatus == HostVerificationStatus.DRAFT
                || currentStatus == HostVerificationStatus.PENDING_EMAIL_VERIFICATION) {
            profile.setVerificationStatus(HostVerificationStatus.PENDING_DOCUMENTS);
        }

        HostProfile saved = hostProfileRepository.save(profile);
        if (currentStatus != saved.getVerificationStatus()) {
            recordStatusChange(saved, currentStatus, saved.getVerificationStatus(), email, "Onboarding draft updated");
        }
        return getOnboardingStatus(email);
    }

    @Transactional
    public Map<String, Object> uploadDocument(String email, String docKind, MultipartFile file) {
        AppUser user = getUserByEmail(email);
        if (!user.isEmailVerified()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Please verify your email first.");
        }

        HostProfile profile = hostProfileRepository.findByUser_Id(user.getId())
                .orElseGet(() -> hostProfileRepository.save(HostProfile.builder()
                        .user(user)
                        .phone(user.getMobileNumber() != null ? user.getMobileNumber() : "")
                        .city("PENDING")
                        .verificationStatus(HostVerificationStatus.PENDING_DOCUMENTS)
                        .active(false)
                        .build()));

        String normalizedKind = docKind == null ? "GOVERNMENT_ID" : docKind.trim().toUpperCase();
        String url = documentStorageService.storeHostDocument(user.getId(), normalizedKind, file);

        if ("PROFILE_PHOTO".equals(normalizedKind)) {
            profile.setProfilePhotoUrl(url);
        } else {
            profile.setDocumentUrl(url);
        }
        hostProfileRepository.save(profile);

        return Map.of(
                "docKind", normalizedKind,
                "documentUrl", url,
                "message", "Document uploaded securely."
        );
    }

    @Transactional
    public Map<String, Object> submitApplication(String email) {
        AppUser user = getUserByEmail(email);
        if (!user.isEmailVerified()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Email verification is required before submitting.");
        }

        HostProfile profile = hostProfileRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please fill out your Host application first."));

        if (profile.getVerificationStatus() == HostVerificationStatus.VERIFIED
                || profile.getVerificationStatus() == HostVerificationStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Your Host account is already verified.");
        }

        if (isBlank(profile.getPhone())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mobile number is required.");
        }
        if (isBlank(profile.getCity()) || "PENDING".equalsIgnoreCase(profile.getCity())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please select an active service city.");
        }
        if (!serviceCityRepository.existsByCityNameIgnoreCaseAndActiveTrue(profile.getCity().trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected city is not an active service city.");
        }
        if (isBlank(profile.getAddress())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Address is required.");
        }
        if (isBlank(profile.getDocumentType()) || isBlank(profile.getGovernmentIdNumber())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Government ID type and number are required.");
        }
        if (isBlank(profile.getDocumentUrl())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please upload your Government ID document.");
        }
        if (!Boolean.TRUE.equals(profile.getTermsAccepted())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You must accept the Host Terms & Conditions.");
        }

        HostVerificationStatus previous = profile.getVerificationStatus();
        profile.setVerificationStatus(HostVerificationStatus.PENDING_ADMIN_APPROVAL);
        profile.setSubmittedAt(Instant.now());
        profile.setRejectionReason(null);
        HostProfile saved = hostProfileRepository.save(profile);

        recordStatusChange(saved, previous, HostVerificationStatus.PENDING_ADMIN_APPROVAL, email, "Submitted for Admin verification");
        auditLogService.log("SUBMIT_HOST_APPLICATION", "HostProfile", String.valueOf(saved.getId()),
                "Host application submitted for city " + saved.getCity());

        emailService.sendEmail(
                user.getEmail(),
                "Host Application Submitted - Car Rental",
                "Hello " + user.getName() + ",\n\n"
                        + "Your Host onboarding application for " + saved.getCity() + " has been submitted and is pending Admin verification.\n"
                        + "You will receive an email once an Admin reviews your documents.\n\n"
                        + "Best regards,\nCar Rental Team"
        );

        return getOnboardingStatus(email);
    }

    public Resource loadDocumentForAuthorizedUser(String requesterEmail, Long targetUserId, String fileName) {
        AppUser requester = getUserByEmail(requesterEmail);
        boolean isOwner = requester.getId().equals(targetUserId);
        boolean isSuperAdmin = requester.hasRole(UserRole.SUPER_ADMIN);
        boolean isAuthorizedCityAdmin = false;

        if (!isOwner && !isSuperAdmin && requester.hasRole(UserRole.ADMIN)) {
            HostProfile host = hostProfileRepository.findByUser_Id(targetUserId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Host not found"));
            adminAuthorizationService.assertCityAuthorized(requester, host.getCity());
            isAuthorizedCityAdmin = true;
        }

        if (!isOwner && !isSuperAdmin && !isAuthorizedCityAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this document.");
        }

        try {
            return documentStorageService.loadHostDocument(targetUserId, fileName);
        } catch (SecurityException ex) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    private void recordStatusChange(HostProfile profile, HostVerificationStatus prev, HostVerificationStatus next, String actor, String remarks) {
        historyRepository.save(HostStatusHistory.builder()
                .hostProfile(profile)
                .previousStatus(prev)
                .newStatus(next)
                .changedBy(actor)
                .remarks(remarks)
                .build());
    }

    private AppUser getUserByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private boolean isBlank(String val) {
        return val == null || val.isBlank();
    }

    private String maskIdentifier(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.length() <= 4) {
            return "****";
        }
        return "*".repeat(trimmed.length() - 4) + trimmed.substring(trimmed.length() - 4);
    }
}
