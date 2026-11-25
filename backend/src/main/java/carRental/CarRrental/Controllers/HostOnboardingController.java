package carRental.CarRrental.Controllers;

import carRental.CarRrental.Services.HostOnboardingService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class HostOnboardingController {

    private final HostOnboardingService hostOnboardingService;

    public HostOnboardingController(HostOnboardingService hostOnboardingService) {
        this.hostOnboardingService = hostOnboardingService;
    }

    @GetMapping("/host-onboarding/status")
    public ResponseEntity<Map<String, Object>> getStatus(Authentication authentication) {
        return ResponseEntity.ok(hostOnboardingService.getOnboardingStatus(authentication.getName()));
    }

    @PostMapping("/host-onboarding/apply")
    public ResponseEntity<Map<String, Object>> saveDraft(
            Authentication authentication,
            @RequestBody Map<String, Object> payload
    ) {
        return ResponseEntity.ok(hostOnboardingService.saveDraft(authentication.getName(), payload));
    }

    @PostMapping(value = "/host-onboarding/documents/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadDocument(
            Authentication authentication,
            @RequestParam(defaultValue = "GOVERNMENT_ID") String docKind,
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(hostOnboardingService.uploadDocument(authentication.getName(), docKind, file));
    }

    @PostMapping("/host-onboarding/submit")
    public ResponseEntity<Map<String, Object>> submitApplication(Authentication authentication) {
        return ResponseEntity.ok(hostOnboardingService.submitApplication(authentication.getName()));
    }

    @GetMapping("/documents/host/{userId}/{fileName}")
    public ResponseEntity<Resource> getHostDocument(
            Authentication authentication,
            @PathVariable Long userId,
            @PathVariable String fileName
    ) {
        Resource resource = hostOnboardingService.loadDocumentForAuthorizedUser(
                authentication.getName(), userId, fileName);

        String lower = fileName.toLowerCase();
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (lower.endsWith(".pdf")) {
            mediaType = MediaType.APPLICATION_PDF;
        } else if (lower.endsWith(".png")) {
            mediaType = MediaType.IMAGE_PNG;
        } else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            mediaType = MediaType.IMAGE_JPEG;
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .body(resource);
    }
}
