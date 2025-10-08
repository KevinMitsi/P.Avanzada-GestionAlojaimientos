package com.avanzada.alojamientos.services.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.avanzada.alojamientos.exceptions.DeletingStorageException;
import com.avanzada.alojamientos.exceptions.UploadingStorageException;
import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.cloudinary.utils.ObjectUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class StorageServiceImplTest {

    @Mock
    private MultipartFile mockMultipartFile;

    @Mock
    private Cloudinary mockCloudinary;

    @Mock
    private Uploader mockUploader;

    private StorageServiceImpl storageService;
    private StorageServiceImpl storageServiceWithDefaults;

    @BeforeEach
    void setUp() {
        // Arrange - Configurar servicio con credenciales válidas
        storageService = new StorageServiceImpl(
                "valid_cloud_name",
                "valid_api_key",
                "valid_api_secret"
        );

        // Configurar servicio con credenciales por defecto (inválidas)
        storageServiceWithDefaults = new StorageServiceImpl(
                "default_cloud_name",
                "default_api_key",
                "default_api_secret"
        );

        // Inyectar el mock de Cloudinary en el servicio válido
        ReflectionTestUtils.setField(storageService, "cloudinary", mockCloudinary);
    }

    @Test
    void constructor_ShouldInitializeCloudinaryConfig_WhenValidCredentials() {
        // Arrange & Act
        StorageServiceImpl service = new StorageServiceImpl(
                "test_cloud",
                "test_key",
                "test_secret"
        );

        // Assert
        assertNotNull(service);
        assertEquals("test_cloud", ReflectionTestUtils.getField(service, "cloudName"));
        assertEquals("test_key", ReflectionTestUtils.getField(service, "apiKey"));
        assertEquals("test_secret", ReflectionTestUtils.getField(service, "apiSecret"));
        assertNotNull(ReflectionTestUtils.getField(service, "cloudinary"));
    }

    @Test
    void upload_ShouldReturnUploadResult_WhenValidFileAndCredentials() throws Exception {
        // Arrange
        byte[] fileContent = "test content".getBytes();
        Map<String, Object> expectedResult = new HashMap<>();
        expectedResult.put("public_id", "test_image_id");
        expectedResult.put("url", "https://cloudinary.com/test_image.jpg");

        when(mockMultipartFile.getOriginalFilename()).thenReturn("test_image.jpg");
        when(mockMultipartFile.getBytes()).thenReturn(fileContent);
        when(mockCloudinary.uploader()).thenReturn(mockUploader);
        when(mockUploader.upload(any(File.class), any(Map.class))).thenReturn(expectedResult);

        // Act
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) storageService.upload(mockMultipartFile);

        // Assert
        assertNotNull(result);
        assertEquals("test_image_id", result.get("public_id"));
        assertEquals("https://cloudinary.com/test_image.jpg", result.get("url"));

        verify(mockMultipartFile).getOriginalFilename();
        verify(mockMultipartFile).getBytes();
        verify(mockCloudinary).uploader();
        verify(mockUploader).upload(any(File.class), eq(ObjectUtils.asMap("folder", StorageServiceImpl.APP_CLOUDINARY_STORAGE)));
    }

    @Test
    void upload_ShouldThrowUploadingStorageException_WhenDefaultCredentials() throws IOException {
        // Arrange
        // No se necesita mockear getOriginalFilename() porque la validación falla antes

        // Act & Assert
        UploadingStorageException exception = assertThrows(UploadingStorageException.class,
                () -> storageServiceWithDefaults.upload(mockMultipartFile));

        assertEquals("Cloudinary credentials not configured. Please set CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, and CLOUDINARY_API_SECRET environment variables.",
                exception.getMessage());

        // No se debe verificar getOriginalFilename() porque la validación falla antes
        verify(mockMultipartFile, never()).getOriginalFilename();
        verify(mockMultipartFile, never()).getBytes();
        verify(mockCloudinary, never()).uploader();
    }

    @Test
    void upload_ShouldThrowUploadingStorageException_WhenCloudinaryThrowsIOException() throws Exception {
        // Arrange
        byte[] fileContent = "test content".getBytes();
        IOException cloudinaryException = new IOException("Cloudinary upload failed");

        when(mockMultipartFile.getOriginalFilename()).thenReturn("test_image.jpg");
        when(mockMultipartFile.getBytes()).thenReturn(fileContent);
        when(mockCloudinary.uploader()).thenReturn(mockUploader);
        when(mockUploader.upload(any(File.class), any(Map.class))).thenThrow(cloudinaryException);

        // Act & Assert
        UploadingStorageException exception = assertThrows(UploadingStorageException.class,
                () -> storageService.upload(mockMultipartFile));

        assertEquals("Cloudinary upload failed", exception.getMessage());

        verify(mockMultipartFile).getOriginalFilename();
        verify(mockMultipartFile).getBytes();
        verify(mockCloudinary).uploader();
        verify(mockUploader).upload(any(File.class), any(Map.class));
    }

    @Test
    void upload_ShouldThrowUploadingStorageException_WhenFileConversionFails() throws Exception {
        // Arrange
        when(mockMultipartFile.getOriginalFilename()).thenReturn("test_image.jpg");
        when(mockMultipartFile.getBytes()).thenThrow(new IOException("File read error"));

        // Act & Assert
        UploadingStorageException exception = assertThrows(UploadingStorageException.class,
                () -> storageService.upload(mockMultipartFile));

        assertEquals("File read error", exception.getMessage());

        verify(mockMultipartFile).getOriginalFilename();
        verify(mockMultipartFile).getBytes();
        // No se debe llamar a cloudinary.uploader() porque falla antes en getBytes()
        verify(mockCloudinary, never()).uploader();
    }

    @Test
    void upload_ShouldHandleNullOriginalFilename() {
        // Arrange
        when(mockMultipartFile.getOriginalFilename()).thenReturn(null);

        // Act & Assert
        assertThrows(NullPointerException.class, () -> storageService.upload(mockMultipartFile));

        verify(mockMultipartFile).getOriginalFilename();
        // No se debe llamar a getBytes() porque falla antes en Objects.requireNonNull()
        verify(mockCloudinary, never()).uploader();
    }

    @Test
    void delete_ShouldReturnDeleteResult_WhenValidImageIdAndCredentials() throws Exception {
        // Arrange
        String imageId = "test_image_id";
        Map<String, Object> expectedResult = new HashMap<>();
        expectedResult.put("result", "ok");

        when(mockCloudinary.uploader()).thenReturn(mockUploader);
        when(mockUploader.destroy(imageId, ObjectUtils.emptyMap())).thenReturn(expectedResult);

        // Act
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) storageService.delete(imageId);

        // Assert
        assertNotNull(result);
        assertEquals("ok", result.get("result"));

        verify(mockCloudinary).uploader();
        verify(mockUploader).destroy(imageId, ObjectUtils.emptyMap());
    }

    @Test
    void delete_ShouldThrowDeletingStorageException_WhenDefaultCredentials() {
        // Arrange
        String imageId = "test_image_id";

        // Act & Assert
        DeletingStorageException exception = assertThrows(DeletingStorageException.class,
                () -> storageServiceWithDefaults.delete(imageId));

        assertEquals("Cloudinary credentials not configured. Please set CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, and CLOUDINARY_API_SECRET environment variables.",
                exception.getMessage());
    }

    @Test
    void delete_ShouldThrowDeletingStorageException_WhenCloudinaryThrowsIOException() throws Exception {
        // Arrange
        String imageId = "test_image_id";
        IOException cloudinaryException = new IOException("Cloudinary delete failed");

        when(mockCloudinary.uploader()).thenReturn(mockUploader);
        when(mockUploader.destroy(imageId, ObjectUtils.emptyMap())).thenThrow(cloudinaryException);

        // Act & Assert
        DeletingStorageException exception = assertThrows(DeletingStorageException.class,
                () -> storageService.delete(imageId));

        assertEquals("Cloudinary delete failed", exception.getMessage());

        verify(mockCloudinary).uploader();
        verify(mockUploader).destroy(imageId, ObjectUtils.emptyMap());
    }

    @Test
    void validateCloudinaryConfig_ShouldThrowUploadingStorageException_WhenCloudNameIsDefault() {
        // Arrange
        StorageServiceImpl serviceWithDefaultCloudName = new StorageServiceImpl(
                "default_cloud_name",
                "valid_api_key",
                "valid_api_secret"
        );

        // Act & Assert
        UploadingStorageException exception = assertThrows(UploadingStorageException.class,
                () -> serviceWithDefaultCloudName.upload(mockMultipartFile));

        assertTrue(exception.getMessage().contains("Cloudinary credentials not configured"));
    }

    @Test
    void validateCloudinaryConfig_ShouldThrowUploadingStorageException_WhenApiKeyIsDefault() {
        // Arrange
        StorageServiceImpl serviceWithDefaultApiKey = new StorageServiceImpl(
                "valid_cloud_name",
                "default_api_key",
                "valid_api_secret"
        );

        // Act & Assert
        UploadingStorageException exception = assertThrows(UploadingStorageException.class,
                () -> serviceWithDefaultApiKey.upload(mockMultipartFile));

        assertTrue(exception.getMessage().contains("Cloudinary credentials not configured"));
    }

    @Test
    void validateCloudinaryConfig_ShouldThrowUploadingStorageException_WhenApiSecretIsDefault() {
        // Arrange
        StorageServiceImpl serviceWithDefaultApiSecret = new StorageServiceImpl(
                "valid_cloud_name",
                "valid_api_key",
                "default_api_secret"
        );

        // Act & Assert
        UploadingStorageException exception = assertThrows(UploadingStorageException.class,
                () -> serviceWithDefaultApiSecret.upload(mockMultipartFile));

        assertTrue(exception.getMessage().contains("Cloudinary credentials not configured"));
    }

    @Test
    void convert_ShouldCreateTemporaryFile_WhenValidMultipartFile() throws Exception {
        // Arrange
        byte[] fileContent = "test file content for conversion".getBytes();
        Map<String, Object> expectedResult = new HashMap<>();
        expectedResult.put("public_id", "converted_file_id");

        when(mockMultipartFile.getOriginalFilename()).thenReturn("test_conversion.jpg");
        when(mockMultipartFile.getBytes()).thenReturn(fileContent);
        when(mockCloudinary.uploader()).thenReturn(mockUploader);
        when(mockUploader.upload(any(File.class), any(Map.class))).thenReturn(expectedResult);

        // Act
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) storageService.upload(mockMultipartFile);

        // Assert
        assertNotNull(result);
        assertEquals("converted_file_id", result.get("public_id"));

        verify(mockMultipartFile).getBytes();
        verify(mockUploader).upload(any(File.class), any(Map.class));
    }

    @Test
    void appCloudinaryStorage_ShouldHaveCorrectConstantValue() {
        // Act & Assert
        assertEquals("app_staygo_project", StorageServiceImpl.APP_CLOUDINARY_STORAGE);
    }

    @Test
    void upload_ShouldUseCorrectFolderInCloudinary() throws Exception {
        // Arrange
        byte[] fileContent = "test content".getBytes();
        Map<String, Object> expectedResult = new HashMap<>();
        expectedResult.put("public_id", "test_image_id");

        when(mockMultipartFile.getOriginalFilename()).thenReturn("test_image.jpg");
        when(mockMultipartFile.getBytes()).thenReturn(fileContent);
        when(mockCloudinary.uploader()).thenReturn(mockUploader);
        when(mockUploader.upload(any(File.class), any(Map.class))).thenReturn(expectedResult);

        // Act
        storageService.upload(mockMultipartFile);

        // Assert
        verify(mockUploader).upload(any(File.class),
                eq(ObjectUtils.asMap("folder", "app_staygo_project")));
    }

    @Test
    void delete_ShouldUseEmptyMapForOptions() throws Exception {
        // Arrange
        String imageId = "test_image_id";
        Map<String, Object> expectedResult = new HashMap<>();
        expectedResult.put("result", "ok");

        when(mockCloudinary.uploader()).thenReturn(mockUploader);
        when(mockUploader.destroy(imageId, ObjectUtils.emptyMap())).thenReturn(expectedResult);

        // Act
        storageService.delete(imageId);

        // Assert
        verify(mockUploader).destroy(imageId, ObjectUtils.emptyMap());
    }
}