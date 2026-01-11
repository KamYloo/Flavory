package com.flavory.userservice.service.impl;

import com.flavory.userservice.exception.FileStorageException;
import com.flavory.userservice.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path fileStorageLocation;

    @Value("${app.file.max-size:10485760}")
    private Long maxFileSize;

    @Value("${app.file.allowed-extensions:jpg,jpeg,png,webp,gif}")
    private String allowedExtensions;

    @Value("${app.file.base-url:http://localhost:8080}")
    private String baseUrl;

    public FileStorageServiceImpl(@Value("${app.file.upload-dir:/uploads/userProfiles}") String uploadDir) {
        try {
            this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Nie można utworzyć katalogu do przechowywania plików", ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file) {
        validateFile(file);

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String fileExtension = getFileExtension(originalFilename);
        String newFilename = generateUniqueFilename(fileExtension);

        try {
            Path targetLocation = this.fileStorageLocation.resolve(newFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/dishes/" + newFilename;

        } catch (IOException ex) {
            throw new FileStorageException("Nie można zapisać pliku " + newFilename, ex);
        }
    }

    @Override
    public String updateFile(MultipartFile file, String existingFileUrl) {
        if (existingFileUrl != null && !existingFileUrl.isEmpty()) {
            deleteFile(existingFileUrl);
        }

        return storeFile(file);
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            if (fileUrl.startsWith(baseUrl)) {
                fileUrl = fileUrl.replace(baseUrl, "");
            }

            String filename = fileUrl.replace("/uploads/dishes/", "");
            Path filePath = this.fileStorageLocation.resolve(filename).normalize();

            if (!filePath.startsWith(this.fileStorageLocation)) {
                throw new FileStorageException("Próba dostępu do pliku poza dozwolonym katalogiem");
            }

            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }

        } catch (IOException ex) {
            throw new FileStorageException("Nie można usunąć pliku: " + fileUrl, ex);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("Nie można zapisać pustego pliku");
        }

        if (file.getSize() > maxFileSize) {
            throw new FileStorageException(
                    String.format("Rozmiar pliku przekracza maksymalny dozwolony rozmiar (%.2f MB). Otrzymano: %.2f MB",
                            maxFileSize / 1024.0 / 1024.0,
                            file.getSize() / 1024.0 / 1024.0)
            );
        }

        String filename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        if (filename.contains("..")) {
            throw new FileStorageException("Nazwa pliku zawiera nieprawidłową ścieżkę: " + filename);
        }

        String extension = getFileExtension(filename).toLowerCase();

        if (extension.isEmpty()) {
            throw new FileStorageException("Plik nie ma rozszerzenia");
        }

        if (!allowedExtensions.contains(extension)) {
            throw new FileStorageException(
                    String.format("Niedozwolony typ pliku '%s'. Dozwolone rozszerzenia: %s",
                            extension, allowedExtensions)
            );
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new FileStorageException("Plik nie jest obrazem. Content-Type: " + contentType);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }

        int lastIndexOf = filename.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }
        return filename.substring(lastIndexOf + 1);
    }

    private String generateUniqueFilename(String extension) {
        return String.format("%s.%s",
                UUID.randomUUID().toString(),
                extension
        );
    }
}
