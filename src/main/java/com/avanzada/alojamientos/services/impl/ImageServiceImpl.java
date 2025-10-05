package com.avanzada.alojamientos.services.impl;


import com.avanzada.alojamientos.exceptions.ConnectionCloudinaryException;
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

    public static final String APP_CLOUDINARY_STORAGE = "app_staygo_project";

    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;
    private final Cloudinary cloudinary;

    public ImageServiceImpl(@Value("${cloudinary.cloud-name}") String cloudName,
                           @Value("${cloudinary.api-key}") String apiKey,
                           @Value("${cloudinary.api-secret}") String apiSecret) {
        this.cloudName = cloudName;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;

        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        this.cloudinary = new Cloudinary(config);
    }

    @Override
    public Map upload(MultipartFile image) throws UploadingImageException {
        File file;
        try {
        validateCloudinaryConfig();
            file = convert(image);
            return cloudinary.uploader().upload(file, ObjectUtils.asMap("folder", APP_CLOUDINARY_STORAGE));
        } catch (IOException ex) {
            throw new UploadingImageException(ex.getMessage());
        }
    }

    @Override
    public Map delete(String imageId) throws DeletingImageException {
        try {
        validateCloudinaryConfig();
            return cloudinary.uploader().destroy(imageId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new DeletingImageException(e.getMessage());
        }
    }

    private void validateCloudinaryConfig() throws ConnectionCloudinaryException {
        if ("default_cloud_name".equals(cloudName) ||
            "default_api_key".equals(apiKey) ||
            "default_api_secret".equals(apiSecret)) {
            throw new ConnectionCloudinaryException("Cloudinary credentials not configured. Please set CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, and CLOUDINARY_API_SECRET environment variables.");
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