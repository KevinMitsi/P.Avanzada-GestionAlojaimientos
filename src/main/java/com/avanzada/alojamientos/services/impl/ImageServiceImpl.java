package com.avanzada.alojamientos.services.impl;


import com.avanzada.alojamientos.exceptions.DeletingImageException;
import com.avanzada.alojamientos.exceptions.UploadingImageException;
import com.avanzada.alojamientos.services.ImageService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class ImageServiceImpl implements ImageService {

    @Value("${cloudinary.cloud_name}")
    private String cloudName;

    @Value("${cloudinary.api_key}")
    private String apiKey;

    @Value("${cloudinary.api_secret}")
    private String apiSecret;

    private final Cloudinary cloudinary;

    public ImageServiceImpl(){
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        cloudinary = new Cloudinary(config);
    }

    @Override
    public Map upload(MultipartFile image) throws UploadingImageException {
        File file;
        try {
            file = convert(image);
            return cloudinary.uploader().upload(file, ObjectUtils.asMap("folder", "app_accommodations"));
        } catch (IOException ex) {
            throw new UploadingImageException(ex.getMessage());
        }
    }

    @Override
    public Map delete(String imageId) throws DeletingImageException {
        try {
            return cloudinary.uploader().destroy(imageId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new DeletingImageException(e.getMessage());
        }
    }

    private File convert(MultipartFile image) throws IOException {
        File file = File.createTempFile(Objects.requireNonNull(image.getOriginalFilename()), null);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(image.getBytes());
        }
        return file;
    }
}