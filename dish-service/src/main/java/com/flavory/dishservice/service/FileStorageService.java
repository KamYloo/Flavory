package com.flavory.dishservice.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface FileStorageService {

    String storeFile(MultipartFile file);
    List<String> storeFiles(List<MultipartFile> files);
    String updateFile(MultipartFile file, String existingFileUrl);
    List<String> updateFiles(List<MultipartFile> newFiles, List<String> existingFileUrls);
    void deleteFile(String fileUrl);
    void deleteFiles(List<String> fileUrls);
    boolean fileExists(String fileUrl);
}