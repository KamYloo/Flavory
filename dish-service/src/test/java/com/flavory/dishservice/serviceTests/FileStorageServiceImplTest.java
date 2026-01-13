package com.flavory.dishservice.serviceTests;

import com.flavory.dishservice.exception.FileStorageException;
import com.flavory.dishservice.service.impl.FileStorageServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("FileStorageServiceImpl Tests")
class FileStorageServiceImplTest {

    @TempDir Path tempDir;

    private FileStorageServiceImpl fileStorageService;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final String ALLOWED_EXTENSIONS = "jpg,jpeg,png,webp,gif";
    private static final String BASE_URL = "http://localhost:8080";

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageServiceImpl(tempDir.toString());
        ReflectionTestUtils.setField(fileStorageService, "maxFileSize", MAX_FILE_SIZE);
        ReflectionTestUtils.setField(fileStorageService, "allowedExtensions", ALLOWED_EXTENSIONS);
        ReflectionTestUtils.setField(fileStorageService, "baseUrl", BASE_URL);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.walk(tempDir)
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException ignored) {
                    }
                });
    }

    private MockMultipartFile createImageFile(String filename, String contentType, byte[] content) {
        return new MockMultipartFile("file", filename, contentType, content);
    }

    private MockMultipartFile createJpegFile(String filename, String content) {
        return createImageFile(filename, "image/jpeg", content.getBytes());
    }

    private MockMultipartFile createJpegFile(String filename) {
        return createJpegFile(filename, "test content");
    }

    private Path getPathFromUrl(String fileUrl) {
        String filename = fileUrl.replace("/uploads/dishes/", "");
        return tempDir.resolve(filename);
    }

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("Should create storage directory")
        void shouldCreateStorageDirectory() {
            Path newDir = tempDir.resolve("new-uploads");

            new FileStorageServiceImpl(newDir.toString());

            assertThat(newDir).exists().isDirectory();
        }

        @Test
        @DisplayName("Should throw exception when cannot create directory")
        void shouldThrowWhenCannotCreateDirectory() {
            assertThatThrownBy(() -> new FileStorageServiceImpl("\0invalid"))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessageContaining("Nie można utworzyć katalogu");
        }
    }

    @Nested
    @DisplayName("storeFile")
    class StoreFileTests {

        @Test
        @DisplayName("Should store valid image file")
        void shouldStoreValidImage() throws IOException {
            MockMultipartFile file = createJpegFile("test.jpg");

            String fileUrl = fileStorageService.storeFile(file);

            assertThat(fileUrl).startsWith("/uploads/dishes/").endsWith(".jpg").contains("-");

            Path savedFile = getPathFromUrl(fileUrl);
            assertThat(savedFile).exists();
            assertThat(Files.size(savedFile)).isEqualTo(file.getSize());
        }

        @ParameterizedTest
        @DisplayName("Should accept all allowed extensions")
        @ValueSource(strings = {"jpg", "jpeg", "png", "webp", "gif"})
        void shouldAcceptAllowedExtensions(String extension) {
            MockMultipartFile file = createImageFile("test." + extension, "image/" + extension, "content".getBytes());

            String fileUrl = fileStorageService.storeFile(file);

            assertThat(fileUrl).endsWith("." + extension);
        }

        @Test
        @DisplayName("Should generate unique filenames")
        void shouldGenerateUniqueFilenames() {
            MockMultipartFile file1 = createJpegFile("image.jpg", "content1");
            MockMultipartFile file2 = createJpegFile("image.jpg", "content2");

            String url1 = fileStorageService.storeFile(file1);
            String url2 = fileStorageService.storeFile(file2);

            assertThat(url1).isNotEqualTo(url2);
        }

        @Test
        @DisplayName("Should preserve extension case")
        void shouldPreserveExtensionCase() {
            MockMultipartFile file = createImageFile("test.JPG", "image/jpeg", "content".getBytes());

            String fileUrl = fileStorageService.storeFile(file);

            assertThat(fileUrl).endsWith(".JPG");
        }

        @Test
        @DisplayName("Should reject null file")
        void shouldRejectNullFile() {
            assertThatThrownBy(() -> fileStorageService.storeFile(null))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessage("Nie można zapisać pustego pliku");
        }

        @Test
        @DisplayName("Should reject empty file")
        void shouldRejectEmptyFile() {
            MockMultipartFile emptyFile = createJpegFile("empty.jpg", "");

            assertThatThrownBy(() -> fileStorageService.storeFile(emptyFile))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessage("Nie można zapisać pustego pliku");
        }

        @Test
        @DisplayName("Should reject oversized file")
        void shouldRejectOversizedFile() {
            byte[] largeContent = new byte[(int) (MAX_FILE_SIZE + 1)];
            MockMultipartFile largeFile = createImageFile("large.jpg", "image/jpeg", largeContent);

            assertThatThrownBy(() -> fileStorageService.storeFile(largeFile))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessageContaining("Rozmiar pliku przekracza");
        }

        @Test
        @DisplayName("Should reject path traversal")
        void shouldRejectPathTraversal() {
            MockMultipartFile file = createJpegFile("../../../etc/passwd");

            assertThatThrownBy(() -> fileStorageService.storeFile(file))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessageContaining("nieprawidłową ścieżkę");
        }

        @Test
        @DisplayName("Should reject file without extension")
        void shouldRejectFileWithoutExtension() {
            MockMultipartFile file = createJpegFile("noextension");

            assertThatThrownBy(() -> fileStorageService.storeFile(file))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessage("Plik nie ma rozszerzenia");
        }

        @ParameterizedTest
        @DisplayName("Should reject disallowed extensions")
        @ValueSource(strings = {"txt", "pdf", "exe", "bat", "sh", "zip"})
        void shouldRejectDisallowedExtensions(String extension) {
            MockMultipartFile file = createJpegFile("file." + extension);

            assertThatThrownBy(() -> fileStorageService.storeFile(file))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessageContaining("Niedozwolony typ pliku");
        }

        @Test
        @DisplayName("Should reject non-image content type")
        void shouldRejectNonImageContentType() {
            MockMultipartFile file = createImageFile("doc.jpg", "application/pdf", "content".getBytes());

            assertThatThrownBy(() -> fileStorageService.storeFile(file))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessageContaining("Plik nie jest obrazem");
        }

        @Test
        @DisplayName("Should reject null content type")
        void shouldRejectNullContentType() {
            MockMultipartFile file = createImageFile("test.jpg", null, "content".getBytes());

            assertThatThrownBy(() -> fileStorageService.storeFile(file))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessageContaining("Plik nie jest obrazem");
        }
    }

    @Nested
    @DisplayName("storeFiles")
    class StoreFilesTests {

        @Test
        @DisplayName("Should store multiple files")
        void shouldStoreMultipleFiles() {
            List<MultipartFile> files = List.of(
                    createJpegFile("image1.jpg"),
                    createJpegFile("image2.jpg"),
                    createJpegFile("image3.jpg")
            );

            List<String> urls = fileStorageService.storeFiles(files);

            assertThat(urls).hasSize(3);
            urls.forEach(url -> assertThat(url).startsWith("/uploads/dishes/"));
        }

        @Test
        @DisplayName("Should return empty list for null files")
        void shouldReturnEmptyListForNull() {
            List<String> urls = fileStorageService.storeFiles(null);

            assertThat(urls).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list for empty list")
        void shouldReturnEmptyListForEmptyList() {
            List<String> urls = fileStorageService.storeFiles(List.of());

            assertThat(urls).isEmpty();
        }

        @Test
        @DisplayName("Should skip empty files")
        void shouldSkipEmptyFiles() {
            List<MultipartFile> files = List.of(
                    createJpegFile("image1.jpg"),
                    createJpegFile("empty.jpg", ""),
                    createJpegFile("image2.jpg")
            );

            List<String> urls = fileStorageService.storeFiles(files);

            assertThat(urls).hasSize(2);
        }
    }

    @Nested
    @DisplayName("updateFile")
    class UpdateFileTests {

        @Test
        @DisplayName("Should update file and delete old one")
        void shouldUpdateAndDeleteOld() throws IOException {
            String oldUrl = fileStorageService.storeFile(createJpegFile("old.jpg", "old"));
            MockMultipartFile newFile = createJpegFile("new.jpg", "new");

            String newUrl = fileStorageService.updateFile(newFile, oldUrl);

            assertThat(newUrl).isNotEqualTo(oldUrl);
            assertThat(getPathFromUrl(oldUrl)).doesNotExist();
            assertThat(getPathFromUrl(newUrl)).exists();
        }

        @Test
        @DisplayName("Should store when URL is null")
        void shouldStoreWhenUrlIsNull() {
            String fileUrl = fileStorageService.updateFile(createJpegFile("new.jpg"), null);

            assertThat(fileUrl).isNotNull().startsWith("/uploads/dishes/");
        }

        @Test
        @DisplayName("Should store when URL is empty")
        void shouldStoreWhenUrlIsEmpty() {
            String fileUrl = fileStorageService.updateFile(createJpegFile("new.jpg"), "");

            assertThat(fileUrl).isNotNull().startsWith("/uploads/dishes/");
        }

        @Test
        @DisplayName("Should handle non-existent old file")
        void shouldHandleNonExistentOldFile() {
            String fileUrl = fileStorageService.updateFile(
                    createJpegFile("new.jpg"),
                    "/uploads/dishes/non-existent.jpg"
            );

            assertThat(fileUrl).isNotNull().startsWith("/uploads/dishes/");
        }
    }

    @Nested
    @DisplayName("updateFiles")
    class UpdateFilesTests {

        @Test
        @DisplayName("Should update multiple files")
        void shouldUpdateMultipleFiles() {
            List<String> oldUrls = List.of(
                    fileStorageService.storeFile(createJpegFile("old1.jpg")),
                    fileStorageService.storeFile(createJpegFile("old2.jpg"))
            );

            List<MultipartFile> newFiles = List.of(
                    createJpegFile("new1.jpg"),
                    createJpegFile("new2.jpg"),
                    createJpegFile("new3.jpg")
            );

            List<String> newUrls = fileStorageService.updateFiles(newFiles, oldUrls);

            assertThat(newUrls).hasSize(3);
            oldUrls.forEach(url -> assertThat(getPathFromUrl(url)).doesNotExist());
        }

        @Test
        @DisplayName("Should store new files when old URLs are null")
        void shouldStoreWhenOldUrlsNull() {
            List<MultipartFile> newFiles = List.of(createJpegFile("new.jpg"));

            List<String> urls = fileStorageService.updateFiles(newFiles, null);

            assertThat(urls).hasSize(1);
        }

        @Test
        @DisplayName("Should store new files when old URLs are empty")
        void shouldStoreWhenOldUrlsEmpty() {
            List<MultipartFile> newFiles = List.of(createJpegFile("new.jpg"));

            List<String> urls = fileStorageService.updateFiles(newFiles, List.of());

            assertThat(urls).hasSize(1);
        }
    }

    @Nested
    @DisplayName("deleteFile")
    class DeleteFileTests {

        @Test
        @DisplayName("Should delete existing file")
        void shouldDeleteExistingFile() throws IOException {
            String fileUrl = fileStorageService.storeFile(createJpegFile("to-delete.jpg"));
            Path filePath = getPathFromUrl(fileUrl);
            assertThat(filePath).exists();

            fileStorageService.deleteFile(fileUrl);

            assertThat(filePath).doesNotExist();
        }

        @Test
        @DisplayName("Should handle non-existent file")
        void shouldHandleNonExistentFile() {
            fileStorageService.deleteFile("/uploads/dishes/non-existent.jpg");
        }

        @Test
        @DisplayName("Should handle null URL")
        void shouldHandleNullUrl() {
            fileStorageService.deleteFile(null);
        }

        @Test
        @DisplayName("Should handle empty URL")
        void shouldHandleEmptyUrl() {
            fileStorageService.deleteFile("");
        }

        @Test
        @DisplayName("Should delete file with base URL prefix")
        void shouldDeleteWithBaseUrlPrefix() throws IOException {
            String fileUrl = fileStorageService.storeFile(createJpegFile("test.jpg"));
            String fullUrl = BASE_URL + fileUrl;
            Path filePath = getPathFromUrl(fileUrl);

            fileStorageService.deleteFile(fullUrl);

            assertThat(filePath).doesNotExist();
        }

        @Test
        @DisplayName("Should reject path traversal")
        void shouldRejectPathTraversal() {
            assertThatThrownBy(() -> fileStorageService.deleteFile("/uploads/dishes/../../../etc/passwd"))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessageContaining("poza dozwolonym katalogiem");
        }
    }

    @Nested
    @DisplayName("deleteFiles")
    class DeleteFilesTests {

        @Test
        @DisplayName("Should delete multiple files")
        void shouldDeleteMultipleFiles() {
            List<String> urls = List.of(
                    fileStorageService.storeFile(createJpegFile("file1.jpg")),
                    fileStorageService.storeFile(createJpegFile("file2.jpg")),
                    fileStorageService.storeFile(createJpegFile("file3.jpg"))
            );

            fileStorageService.deleteFiles(urls);

            urls.forEach(url -> assertThat(getPathFromUrl(url)).doesNotExist());
        }

        @Test
        @DisplayName("Should handle null list")
        void shouldHandleNullList() {
            fileStorageService.deleteFiles(null);
        }

        @Test
        @DisplayName("Should handle empty list")
        void shouldHandleEmptyList() {
            fileStorageService.deleteFiles(List.of());
        }

        @Test
        @DisplayName("Should continue deleting on error")
        void shouldContinueOnError() {
            List<String> urls = List.of(
                    fileStorageService.storeFile(createJpegFile("file1.jpg")),
                    "/uploads/dishes/non-existent.jpg",
                    fileStorageService.storeFile(createJpegFile("file2.jpg"))
            );

            fileStorageService.deleteFiles(urls);

            assertThat(getPathFromUrl(urls.get(0))).doesNotExist();
            assertThat(getPathFromUrl(urls.get(2))).doesNotExist();
        }
    }

    @Nested
    @DisplayName("fileExists")
    class FileExistsTests {

        @Test
        @DisplayName("Should return true for existing file")
        void shouldReturnTrueForExistingFile() {
            String fileUrl = fileStorageService.storeFile(createJpegFile("test.jpg"));

            boolean exists = fileStorageService.fileExists(fileUrl);

            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should return false for non-existent file")
        void shouldReturnFalseForNonExistent() {
            boolean exists = fileStorageService.fileExists("/uploads/dishes/non-existent.jpg");

            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("Should return false for null URL")
        void shouldReturnFalseForNull() {
            boolean exists = fileStorageService.fileExists(null);

            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("Should return false for empty URL")
        void shouldReturnFalseForEmpty() {
            boolean exists = fileStorageService.fileExists("");

            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("Should handle base URL prefix")
        void shouldHandleBaseUrlPrefix() {
            String fileUrl = fileStorageService.storeFile(createJpegFile("test.jpg"));
            String fullUrl = BASE_URL + fileUrl;

            boolean exists = fileStorageService.fileExists(fullUrl);

            assertThat(exists).isTrue();
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle special characters in filename")
        void shouldHandleSpecialCharacters() {
            String fileUrl = fileStorageService.storeFile(createJpegFile("test image (1).jpg"));

            assertThat(fileUrl).endsWith(".jpg");
        }

        @ParameterizedTest
        @DisplayName("Should handle different extension cases")
        @ValueSource(strings = {"PNG", "JpG", "jpeg", "WebP"})
        void shouldHandleDifferentExtensionCases(String extension) {
            MockMultipartFile file = createImageFile("test." + extension, "image/jpeg", "content".getBytes());

            String fileUrl = fileStorageService.storeFile(file);

            assertThat(fileUrl).endsWith("." + extension);
        }

        @Test
        @DisplayName("Should handle file at exact max size")
        void shouldHandleMaxSizeFile() {
            byte[] maxContent = new byte[(int) MAX_FILE_SIZE];
            MockMultipartFile file = createImageFile("maxsize.jpg", "image/jpeg", maxContent);

            String fileUrl = fileStorageService.storeFile(file);

            assertThat(fileUrl).isNotNull();
        }

        @Test
        @DisplayName("Should complete full lifecycle with multiple files")
        void shouldCompleteFullLifecycle() throws IOException {
            List<MultipartFile> files = List.of(
                    createJpegFile("file1.jpg"),
                    createJpegFile("file2.jpg")
            );
            List<String> urls = fileStorageService.storeFiles(files);
            assertThat(urls).hasSize(2);
            urls.forEach(url -> assertThat(getPathFromUrl(url)).exists());

            List<MultipartFile> newFiles = List.of(createJpegFile("file3.jpg"));
            List<String> newUrls = fileStorageService.updateFiles(newFiles, urls);
            assertThat(newUrls).hasSize(1);
            urls.forEach(url -> assertThat(getPathFromUrl(url)).doesNotExist());
            assertThat(getPathFromUrl(newUrls.getFirst())).exists();

            fileStorageService.deleteFiles(newUrls);
            newUrls.forEach(url -> assertThat(getPathFromUrl(url)).doesNotExist());
        }

        @Test
        @DisplayName("Should verify file existence after operations")
        void shouldVerifyFileExistence() {
            String url = fileStorageService.storeFile(createJpegFile("test.jpg"));

            assertThat(fileStorageService.fileExists(url)).isTrue();

            fileStorageService.deleteFile(url);

            assertThat(fileStorageService.fileExists(url)).isFalse();
        }
    }
}