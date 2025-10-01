package com.avanzada.alojamientos.services;

import com.avanzada.alojamientos.exceptions.DeletingImageException;
import com.avanzada.alojamientos.exceptions.UploadingImageException;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

public interface ImageService {
    Map<Object, Object> upload(MultipartFile image) throws UploadingImageException;
    Map<Object, Object> delete(String imageId) throws DeletingImageException;
}