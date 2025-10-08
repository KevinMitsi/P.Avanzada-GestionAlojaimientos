package com.avanzada.alojamientos.services.impl;

import com.avanzada.alojamientos.entities.UserEntity;
import com.avanzada.alojamientos.exceptions.DeletingStorageException;
import com.avanzada.alojamientos.exceptions.UploadingStorageException;
import com.avanzada.alojamientos.exceptions.UserNotFoundException;
import com.avanzada.alojamientos.repositories.UserRepository;
import com.avanzada.alojamientos.services.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDocumentServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StorageService storageService;

    @Mock
    private MultipartFile multipartFile1;

    @Mock
    private MultipartFile multipartFile2;

    @InjectMocks
    private UserDocumentServiceImpl userDocumentService;

    private UserEntity userEntity;
    private List<String> existingDocuments;
    private Map<Object, Object> uploadResult;

    @BeforeEach
    void setUp() {
        // Arrange - Configuración común para todos los tests
        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setName("Test User");
        userEntity.setEmail("test@example.com");

        existingDocuments = new ArrayList<>();
        existingDocuments.add("https://example.com/existing-document1.pdf");
        existingDocuments.add("https://example.com/existing-document2.pdf");

        uploadResult = new HashMap<>();
        uploadResult.put("secure_url", "https://example.com/uploaded-document.pdf");
    }

    // ========== TESTS PARA uploadDocuments ==========

    @Test
    void uploadDocuments_WithNullDocumentFiles_ShouldReturnEmptyList() {
        // Arrange
        Long userId = 1L;

        // Act
        List<String> result = userDocumentService.uploadDocuments(userId,null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verifyNoInteractions(userRepository, storageService);
    }

    @Test
    void uploadDocuments_WithEmptyDocumentFiles_ShouldReturnEmptyList() {
        // Arrange
        Long userId = 1L;
        List<MultipartFile> documentFiles = Collections.emptyList();

        // Act
        List<String> result = userDocumentService.uploadDocuments(userId, documentFiles);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verifyNoInteractions(userRepository, storageService);
    }

    @Test
    void uploadDocuments_WithValidFiles_ShouldUploadAndReturnUrls() throws UploadingStorageException {
        // Arrange
        Long userId = 1L;
        List<MultipartFile> documentFiles = Arrays.asList(multipartFile1, multipartFile2);
        userEntity.setDocumentsUrl(new ArrayList<>(existingDocuments));

        Map<Object, Object> uploadResult1 = new HashMap<>();
        uploadResult1.put("secure_url", "https://example.com/new-document1.pdf");

        Map<Object, Object> uploadResult2 = new HashMap<>();
        uploadResult2.put("secure_url", "https://example.com/new-document2.pdf");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(storageService.upload(multipartFile1)).thenReturn(uploadResult1);
        when(storageService.upload(multipartFile2)).thenReturn(uploadResult2);
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        // Act
        List<String> result = userDocumentService.uploadDocuments(userId, documentFiles);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("https://example.com/new-document1.pdf", result.get(0));
        assertEquals("https://example.com/new-document2.pdf", result.get(1));

        // Verificar que los documentos se agregaron a la lista existente
        verify(userRepository).save(userEntity);
        assertEquals(4, userEntity.getDocumentsUrl().size()); // 2 existentes + 2 nuevos
        assertTrue(userEntity.getDocumentsUrl().contains("https://example.com/new-document1.pdf"));
        assertTrue(userEntity.getDocumentsUrl().contains("https://example.com/new-document2.pdf"));
    }

    @Test
    void uploadDocuments_WithUserNotFound_ShouldThrowUserNotFoundException() {
        // Arrange
        Long userId = 999L;
        List<MultipartFile> documentFiles = List.of(multipartFile1);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(
            UserNotFoundException.class,
            () -> userDocumentService.uploadDocuments(userId, documentFiles)
        );

        assertEquals("User not found with ID: " + userId, exception.getMessage());
        verify(userRepository).findById(userId);
        verifyNoInteractions(storageService);
        verify(userRepository, never()).save(any());
    }

    @Test
    void uploadDocuments_WithStorageServiceError_ShouldThrowUploadingStorageException() throws UploadingStorageException {
        // Arrange
        Long userId = 1L;
        List<MultipartFile> documentFiles = List.of(multipartFile1);
        userEntity.setDocumentsUrl(new ArrayList<>(existingDocuments));

        UploadingStorageException storageException = new UploadingStorageException("Storage service error");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(storageService.upload(multipartFile1)).thenThrow(storageException);

        // Act & Assert
        UploadingStorageException exception = assertThrows(
            UploadingStorageException.class,
            () -> userDocumentService.uploadDocuments(userId, documentFiles)
        );

        assertEquals("Storage service error", exception.getMessage());
        verify(storageService).upload(multipartFile1);
        verify(userRepository, never()).save(any());
    }

    @Test
    void uploadDocuments_WithUserHavingNullDocumentsList_ShouldCreateNewList() throws UploadingStorageException {
        // Arrange
        Long userId = 1L;
        List<MultipartFile> documentFiles = List.of(multipartFile1);
        userEntity.setDocumentsUrl(null); // Usuario sin documentos previos

        Map<Object, Object> uploadResultLocal = new HashMap<>();
        uploadResultLocal.put("secure_url", "https://example.com/first-document.pdf");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(storageService.upload(multipartFile1)).thenReturn(uploadResultLocal);
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        // Act
        List<String> result = userDocumentService.uploadDocuments(userId, documentFiles);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("https://example.com/first-document.pdf", result.getFirst());

        // Verificar que se creó una nueva lista
        assertNotNull(userEntity.getDocumentsUrl());
        assertEquals(1, userEntity.getDocumentsUrl().size());
        assertEquals("https://example.com/first-document.pdf", userEntity.getDocumentsUrl().getFirst());
    }

    // ========== TESTS PARA deleteDocument ==========

    @Test
    void deleteDocument_WithNullDocumentIndex_ShouldThrowDeletingStorageException() {
        // Arrange
        Long userId = 1L;


        // Act & Assert
        DeletingStorageException exception = assertThrows(
            DeletingStorageException.class,
            () -> userDocumentService.deleteDocument(userId, null)
        );

        assertEquals("documentIndex is required", exception.getMessage());
        verifyNoInteractions(userRepository);
    }

    @Test
    void deleteDocument_WithUserNotFound_ShouldThrowUserNotFoundException() {
        // Arrange
        Long userId = 999L;
        Long documentIndex = 0L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(
            UserNotFoundException.class,
            () -> userDocumentService.deleteDocument(userId, documentIndex)
        );

        assertEquals("User not found with ID: " + userId, exception.getMessage());
        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteDocument_WithNegativeIndex_ShouldThrowDeletingStorageException() {
        // Arrange
        Long userId = 1L;
        Long documentIndex = -1L;
        userEntity.setDocumentsUrl(new ArrayList<>(existingDocuments));

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));

        // Act & Assert
        DeletingStorageException exception = assertThrows(
            DeletingStorageException.class,
            () -> userDocumentService.deleteDocument(userId, documentIndex)
        );

        assertEquals("Invalid document index: " + documentIndex, exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteDocument_WithIndexOutOfBounds_ShouldThrowDeletingStorageException() {
        // Arrange
        Long userId = 1L;
        Long documentIndex = 5L; // Index mayor que el tamaño de la lista
        userEntity.setDocumentsUrl(new ArrayList<>(existingDocuments)); // Solo tiene 2 documentos

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));

        // Act & Assert
        DeletingStorageException exception = assertThrows(
            DeletingStorageException.class,
            () -> userDocumentService.deleteDocument(userId, documentIndex)
        );

        assertEquals("Invalid document index: " + documentIndex, exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteDocument_WithValidIndex_ShouldDeleteDocumentSuccessfully() {
        // Arrange
        Long userId = 1L;
        Long documentIndex = 0L;
        List<String> userDocuments = new ArrayList<>(existingDocuments);
        userEntity.setDocumentsUrl(userDocuments);
        String documentToDelete = userDocuments.getFirst();

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        // Act
        assertDoesNotThrow(() -> userDocumentService.deleteDocument(userId, documentIndex));

        // Assert
        verify(userRepository).findById(userId);
        verify(userRepository).save(userEntity);

        // Verificar que el documento fue eliminado
        assertEquals(1, userEntity.getDocumentsUrl().size());
        assertFalse(userEntity.getDocumentsUrl().contains(documentToDelete));
        assertEquals("https://example.com/existing-document2.pdf", userEntity.getDocumentsUrl().getFirst());
    }

    @Test
    void deleteDocument_WithMiddleIndex_ShouldDeleteCorrectDocument() {
        // Arrange
        Long userId = 1L;
        Long documentIndex = 1L;
        List<String> userDocuments = new ArrayList<>();
        userDocuments.add("document1.pdf");
        userDocuments.add("document2.pdf");
        userDocuments.add("document3.pdf");
        userEntity.setDocumentsUrl(userDocuments);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        // Act
        assertDoesNotThrow(() -> userDocumentService.deleteDocument(userId, documentIndex));

        // Assert
        assertEquals(2, userEntity.getDocumentsUrl().size());
        assertTrue(userEntity.getDocumentsUrl().contains("document1.pdf"));
        assertTrue(userEntity.getDocumentsUrl().contains("document3.pdf"));
        assertFalse(userEntity.getDocumentsUrl().contains("document2.pdf"));
    }

    @Test
    void deleteDocument_WithEmptyDocumentsList_ShouldThrowDeletingStorageException() {
        // Arrange
        Long userId = 1L;
        Long documentIndex = 0L;
        userEntity.setDocumentsUrl(new ArrayList<>()); // Lista vacía

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));

        // Act & Assert
        DeletingStorageException exception = assertThrows(
            DeletingStorageException.class,
            () -> userDocumentService.deleteDocument(userId, documentIndex)
        );

        assertEquals("Invalid document index: " + documentIndex, exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteDocument_WithNullDocumentsList_ShouldThrowDeletingStorageException() {
        // Arrange
        Long userId = 1L;
        Long documentIndex = 0L;
        userEntity.setDocumentsUrl(null); // Lista null

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));

        // Act & Assert
        DeletingStorageException exception = assertThrows(
            DeletingStorageException.class,
            () -> userDocumentService.deleteDocument(userId, documentIndex)
        );

        assertEquals("Invalid document index: " + documentIndex, exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteDocument_WithRepositorySaveException_ShouldThrowDeletingStorageException() {
        // Arrange
        Long userId = 1L;
        Long documentIndex = 0L;
        userEntity.setDocumentsUrl(new ArrayList<>(existingDocuments));

        RuntimeException repositoryException = new RuntimeException("Database error");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(any(UserEntity.class))).thenThrow(repositoryException);

        // Act & Assert
        DeletingStorageException exception = assertThrows(
            DeletingStorageException.class,
            () -> userDocumentService.deleteDocument(userId, documentIndex)
        );

        assertEquals("Error deleting document: Database error", exception.getMessage());
        verify(userRepository).save(userEntity);
    }

    // ========== TESTS PARA MÉTODOS PRIVADOS (indirectos) ==========

    @Test
    void findUserEntity_WithValidUserId_ShouldReturnUser() throws UploadingStorageException {
        // Arrange
        Long userId = 1L;
        List<MultipartFile> documentFiles = List.of(multipartFile1);
        userEntity.setDocumentsUrl(new ArrayList<>());

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(storageService.upload(multipartFile1)).thenReturn(uploadResult);
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        // Act
        List<String> result = userDocumentService.uploadDocuments(userId, documentFiles);

        // Assert
        assertNotNull(result);
        verify(userRepository).findById(userId);
    }

    @Test
    void getCurrentDocuments_WithNullDocumentsList_ShouldReturnEmptyList() throws UploadingStorageException {
        // Arrange
        Long userId = 1L;
        List<MultipartFile> documentFiles = List.of(multipartFile1);
        userEntity.setDocumentsUrl(null); // Lista null para probar getCurrentDocuments

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(storageService.upload(multipartFile1)).thenReturn(uploadResult);
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        // Act
        List<String> result = userDocumentService.uploadDocuments(userId, documentFiles);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        // Verificar que getCurrentDocuments manejó correctamente la lista null
        assertNotNull(userEntity.getDocumentsUrl());
        assertEquals(1, userEntity.getDocumentsUrl().size());
    }
}
