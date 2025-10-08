package com.avanzada.alojamientos.services;

import com.avanzada.alojamientos.exceptions.DeletingStorageException;
import com.avanzada.alojamientos.exceptions.UploadingStorageException;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

public interface StorageService {
    Map<Object, Object> upload(MultipartFile image) throws UploadingStorageException;
    Map<Object, Object> delete(String imageId) throws DeletingStorageException;
}