package carRental.CarRrental.Services;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class DocumentStorageService {

    private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "jpg", "jpeg", "png", "webp");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final Path rootDir;

    public DocumentStorageService() {
        this.rootDir = Paths.get("private-uploads", "host-docs").toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootDir);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not initialize private document storage directory", ex);
        }
    }

    public String storeHostDocument(Long userId, String docKind, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty.");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("File size exceeds 5 MB limit.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Unsupported file type. Allowed: PDF, JPG, JPEG, PNG, WEBP.");
        }

        String originalName = file.getOriginalFilename() == null ? "document" : file.getOriginalFilename();
        String ext = extractExtension(originalName);
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException("Unsupported file extension. Allowed: pdf, jpg, jpeg, png, webp.");
        }

        String sanitizedKind = docKind.replaceAll("[^a-zA-Z0-9_-]", "");
        String storedFileName = "user-" + userId + "-" + sanitizedKind + "-" + UUID.randomUUID() + "." + ext;
        Path userDir = rootDir.resolve("user-" + userId).normalize();
        if (!userDir.startsWith(rootDir)) {
            throw new SecurityException("Invalid storage path.");
        }

        try {
            Files.createDirectories(userDir);
            Path target = userDir.resolve(storedFileName).normalize();
            if (!target.startsWith(userDir)) {
                throw new SecurityException("Invalid file path.");
            }
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return "/api/documents/host/" + userId + "/" + storedFileName;
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to store host document", ex);
        }
    }

    public Resource loadHostDocument(Long userId, String fileName) {
        if (fileName == null || fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            throw new SecurityException("Invalid document filename.");
        }
        Path userDir = rootDir.resolve("user-" + userId).normalize();
        Path filePath = userDir.resolve(fileName).normalize();
        if (!filePath.startsWith(userDir) || !Files.exists(filePath)) {
            throw new IllegalArgumentException("Document not found.");
        }
        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalArgumentException("Document not readable.");
            }
            return resource;
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("Invalid document path.", ex);
        }
    }

    private String extractExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        if (idx < 0 || idx == filename.length() - 1) {
            return "";
        }
        return filename.substring(idx + 1).toLowerCase();
    }
}
