package carRental.CarRrental.Services;

import carRental.CarRrental.Models.AuditLog;
import carRental.CarRrental.Repositories.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void log(String action, String entityType, String entityId, String details) {
        String email = "SYSTEM";
        String role = "SYSTEM";

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            email = auth.getName();
            role = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("UNKNOWN");
        }

        AuditLog entry = AuditLog.builder()
                .actorEmail(email)
                .actorRole(role)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .build();

        auditLogRepository.save(entry);
    }

    public Page<AuditLog> getRecentLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
}
