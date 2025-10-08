package com.avanzada.alojamientos.services;

import com.avanzada.alojamientos.exceptions.DeletingStorageException;
import com.avanzada.alojamientos.exceptions.UploadingStorageException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserDocumentService {

    List<String> uploadDocuments(Long userId, List<MultipartFile> documentFiles) throws UploadingStorageException;
    void deleteDocument(Long userId, Long documentIndex) throws DeletingStorageException;


}
