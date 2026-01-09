package com.flavory.userservice.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String storeFile(MultipartFile file);
    String updateFile(MultipartFile file, String existingFileUrl);
    void deleteFile(String fileUrl);
}
