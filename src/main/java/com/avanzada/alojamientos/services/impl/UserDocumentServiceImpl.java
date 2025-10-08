package com.avanzada.alojamientos.services.impl;

import com.avanzada.alojamientos.entities.UserEntity;
import com.avanzada.alojamientos.exceptions.DeletingStorageException;
import com.avanzada.alojamientos.exceptions.UploadingStorageException;
import com.avanzada.alojamientos.exceptions.UserNotFoundException;
import com.avanzada.alojamientos.repositories.UserRepository;
import com.avanzada.alojamientos.services.StorageService;
import com.avanzada.alojamientos.services.UserDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserDocumentServiceImpl implements UserDocumentService {

    private final UserRepository userRepository;
    private final StorageService storageService;

    @Override
    @Transactional
    public List<String> uploadDocuments(Long userId, List<MultipartFile> documentFiles) throws UploadingStorageException {
        if (documentFiles == null || documentFiles.isEmpty()) {
            return Collections.emptyList();
        }

        UserEntity user = findUserEntity(userId);
        List<String> currentDocuments = getCurrentDocuments(user);
        List<String> uploadedUrls = new ArrayList<>();

        for (MultipartFile file : documentFiles) {
            try {
                Map<Object, Object> uploadResult = storageService.upload(file);
                String documentUrl = (String) uploadResult.get("secure_url");

                currentDocuments.add(documentUrl);
                uploadedUrls.add(documentUrl);

                log.info("Document uploaded successfully for user {}: {}", userId, documentUrl);

            } catch (UploadingStorageException e) {
                log.error("Error uploading document for user {}: {}", userId, e.getMessage());
                throw e;
            }
        }

        user.setDocumentsUrl(currentDocuments);
        userRepository.save(user);

        return uploadedUrls;
    }

    @Override
    @Transactional
    public void deleteDocument(Long userId, Long documentIndex) throws DeletingStorageException {
        if (documentIndex == null) {
            throw new DeletingStorageException("documentIndex is required");
        }

        UserEntity user = findUserEntity(userId);
        List<String> userDocuments = getCurrentDocuments(user);

        if (documentIndex < 0 || documentIndex >= userDocuments.size()) {
            throw new DeletingStorageException("Invalid document index: " + documentIndex);
        }

        try {
            // Eliminar de la lista del usuario
            userDocuments.remove(documentIndex.intValue());
            user.setDocumentsUrl(userDocuments);
            userRepository.save(user);

            log.info("Successfully deleted document at index {} from user {}", documentIndex, userId);

        } catch (Exception e) {
            log.error("Error deleting document: {}", e.getMessage());
            throw new DeletingStorageException("Error deleting document: " + e.getMessage());
        }
    }


    // Métodos privados de utilidad

    private UserEntity findUserEntity(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
    }

    private List<String> getCurrentDocuments(UserEntity user) {
        List<String> documents = user.getDocumentsUrl();
        return documents != null ? new ArrayList<>(documents) : new ArrayList<>();
    }

}
