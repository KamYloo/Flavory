package com.flavory.userservice.ServiceTests;

import com.flavory.userservice.exception.FileStorageException;
import com.flavory.userservice.service.impl.FileStorageServiceImpl;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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

            assertThat(Files.exists(newDir)).isTrue();
            assertThat(Files.isDirectory(newDir)).isTrue();
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
        @DisplayName("Should throw exception for null file")
        void shouldRejectNullFile() {
            assertThatThrownBy(() -> fileStorageService.storeFile(null))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessage("Nie można zapisać pustego pliku");
        }

        @Test
        @DisplayName("Should throw exception for empty file")
        void shouldRejectEmptyFile() {
            MockMultipartFile emptyFile = createJpegFile("empty.jpg", "");

            assertThatThrownBy(() -> fileStorageService.storeFile(emptyFile))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessage("Nie można zapisać pustego pliku");
        }

        @Test
        @DisplayName("Should throw exception when file exceeds max size")
        void shouldRejectOversizedFile() {
            byte[] largeContent = new byte[(int) (MAX_FILE_SIZE + 1)];
            MockMultipartFile largeFile = createImageFile("large.jpg", "image/jpeg", largeContent);

            assertThatThrownBy(() -> fileStorageService.storeFile(largeFile))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessageContaining("Rozmiar pliku przekracza");
        }

        @Test
        @DisplayName("Should throw exception for path traversal")
        void shouldRejectPathTraversal() {
            MockMultipartFile file = createJpegFile("../../../etc/passwd");

            assertThatThrownBy(() -> fileStorageService.storeFile(file))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessageContaining("nieprawidłową ścieżkę");
        }

        @Test
        @DisplayName("Should throw exception for file without extension")
        void shouldRejectFileWithoutExtension() {
            MockMultipartFile file = createJpegFile("noextension");

            assertThatThrownBy(() -> fileStorageService.storeFile(file))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessage("Plik nie ma rozszerzenia");
        }

        @ParameterizedTest
        @DisplayName("Should throw exception for disallowed extensions")
        @ValueSource(strings = {"txt", "pdf", "exe", "bat", "sh", "zip"})
        void shouldRejectDisallowedExtensions(String extension) {
            MockMultipartFile file = createJpegFile("file." + extension);

            assertThatThrownBy(() -> fileStorageService.storeFile(file))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessageContaining("Niedozwolony typ pliku");
        }

        @Test
        @DisplayName("Should throw exception for non-image content type")
        void shouldRejectNonImageContentType() {
            MockMultipartFile file = createImageFile("doc.jpg", "application/pdf", "content".getBytes());

            assertThatThrownBy(() -> fileStorageService.storeFile(file))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessageContaining("Plik nie jest obrazem");
        }

        @Test
        @DisplayName("Should throw exception for null content type")
        void shouldRejectNullContentType() {
            MockMultipartFile file = createImageFile("test.jpg", null, "content".getBytes());

            assertThatThrownBy(() -> fileStorageService.storeFile(file))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessageContaining("Plik nie jest obrazem");
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
        @DisplayName("Should store new file when URL is null")
        void shouldStoreWhenUrlIsNull() {
            String fileUrl = fileStorageService.updateFile(createJpegFile("new.jpg"), null);

            assertThat(fileUrl).isNotNull().startsWith("/uploads/dishes/");
        }

        @Test
        @DisplayName("Should store new file when URL is empty")
        void shouldStoreWhenUrlIsEmpty() {
            String fileUrl = fileStorageService.updateFile(createJpegFile("new.jpg"), "");

            assertThat(fileUrl).isNotNull().startsWith("/uploads/dishes/");
        }

        @Test
        @DisplayName("Should handle update when old file doesn't exist")
        void shouldHandleNonExistentOldFile() {
            String fileUrl = fileStorageService.updateFile(
                    createJpegFile("new.jpg"),
                    "/uploads/dishes/non-existent.jpg"
            );

            assertThat(fileUrl).isNotNull().startsWith("/uploads/dishes/");
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
        @DisplayName("Should handle deletion of non-existent file")
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
        @DisplayName("Should throw exception for path traversal")
        void shouldRejectPathTraversal() {
            assertThatThrownBy(() -> fileStorageService.deleteFile("/uploads/dishes/../../../etc/passwd"))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessageContaining("poza dozwolonym katalogiem");
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
        @DisplayName("Should complete full lifecycle")
        void shouldCompleteFullLifecycle() throws IOException {
            String url1 = fileStorageService.storeFile(createJpegFile("original.jpg", "v1"));
            Path path1 = getPathFromUrl(url1);
            assertThat(path1).exists();

            String url2 = fileStorageService.updateFile(createJpegFile("updated.jpg", "v2"), url1);
            Path path2 = getPathFromUrl(url2);
            assertThat(url2).isNotEqualTo(url1);
            assertThat(path1).doesNotExist();
            assertThat(path2).exists();

            fileStorageService.deleteFile(url2);
            assertThat(path2).doesNotExist();
        }
    }
}