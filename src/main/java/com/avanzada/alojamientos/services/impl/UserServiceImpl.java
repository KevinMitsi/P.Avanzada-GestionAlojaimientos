package com.avanzada.alojamientos.services.impl;

import com.avanzada.alojamientos.DTO.user.CreateUserDTO;
import com.avanzada.alojamientos.DTO.user.EditUserDTO;
import com.avanzada.alojamientos.DTO.auth.RegisterUserDTO;
import com.avanzada.alojamientos.DTO.user.UserDTO;
import com.avanzada.alojamientos.DTO.model.UserRole;
import com.avanzada.alojamientos.entities.HostProfileEntity;
import com.avanzada.alojamientos.entities.UserEntity;
import com.avanzada.alojamientos.entities.ImageEntity;
import com.avanzada.alojamientos.exceptions.UserNotFoundException;
import com.avanzada.alojamientos.exceptions.InvalidPasswordException;
import com.avanzada.alojamientos.exceptions.UploadingStorageException;
import com.avanzada.alojamientos.exceptions.DeletingStorageException;
import com.avanzada.alojamientos.repositories.UserRepository;
import com.avanzada.alojamientos.repositories.HostProfileRepository;
import com.avanzada.alojamientos.repositories.ImageRepository;
import com.avanzada.alojamientos.mappers.UserMapper;
import com.avanzada.alojamientos.services.UserService;
import com.avanzada.alojamientos.services.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    public static final String USER_NOT_FOUND_EXCEPTION_MESSAGE = "Usuario no encontrado con ID: ";
    private final UserRepository userRepository;
    private final HostProfileRepository hostProfileRepository;
    private final ImageRepository imageRepository;
    private final StorageService storageService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserDTO register(RegisterUserDTO dto) {
        log.info("Registrando nuevo usuario con email: {}", dto.email());

        // Verificar que el email no esté en uso
        if (userRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        // Crear entidad desde DTO
        UserEntity user = userMapper.toEntity(dto);

        // Encriptar contraseña
        user.setPassword(passwordEncoder.encode(dto.password()));

        // Establecer rol por defecto como USER si no se especifica
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.addRole(UserRole.USER);
        }

        // Establecer descripción por defecto
        if (user.getDescription() == null || user.getDescription().trim().isEmpty()) {
            user.setDescription("Usuario registrado en la plataforma");
        }

        // Establecer valores por defecto
        user.setCreatedAt(LocalDateTime.now());
        user.setVerified(false);
        user.setEnabled(true);
        user.setDeleted(false);

        // Guardar usuario
        UserEntity savedUser = userRepository.save(user);
        log.info("Usuario registrado exitosamente con ID: {}", savedUser.getId());

        return userMapper.toUserDTO(savedUser);
    }

    @Override
    @Transactional
    public UserDTO create(CreateUserDTO dto) {
        log.info("Creando nuevo usuario con email: {}", dto.email());

        // Verificar que el email no esté en uso
        if (userRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        // Crear entidad desde DTO
        UserEntity user = userMapper.toEntity(dto);

        // Encriptar contraseña
        user.setPassword(passwordEncoder.encode(dto.password()));

        // Establecer valores por defecto
        user.setCreatedAt(LocalDateTime.now());
        user.setVerified(false);
        user.setEnabled(true);
        user.setDeleted(false);

        // Guardar usuario
        UserEntity savedUser = userRepository.save(user);
        log.info("Usuario creado exitosamente con ID: {}", savedUser.getId());

        return userMapper.toUserDTO(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDTO> findById(Long id) {
        log.debug("Buscando usuario con ID: {}", id);

        return userRepository.findById(id)
                .filter(user -> !user.getDeleted())
                .map(userMapper::toUserDTO);
    }

    @Override
    @Transactional
    public void enable(String userId, boolean enable) {
        log.info("Cambiando estado de usuario {} a: {}", userId, enable);

        Long id = Long.parseLong(userId);
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_EXCEPTION_MESSAGE + userId));

        user.setEnabled(enable);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("Estado del usuario {} cambiado a: {}", userId, enable);
    }


    @Override
    @Transactional
    public void changePassword(Long id, String oldPassword, String newPassword) {
        log.info("Cambiando contraseña para usuario: {}", id);

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_EXCEPTION_MESSAGE + id));

        // Verificar contraseña actual
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new InvalidPasswordException("La contraseña actual no es correcta");
        }

        // Actualizar contraseña
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("Contraseña actualizada exitosamente para usuario: {}", id);
    }

    @Override
    @Transactional
    public UserDTO editProfile(Long userId, EditUserDTO dto) {
        log.info("Editando perfil del usuario con ID: {}", userId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_EXCEPTION_MESSAGE + userId));

        // Actualizar campos desde DTO
        userMapper.updateEntityFromDTO(dto, user);
        user.setUpdatedAt(LocalDateTime.now());

        // Guardar cambios
        UserEntity updatedUser = userRepository.save(user);
        log.info("Perfil actualizado exitosamente para usuario con ID: {}", updatedUser.getId());

        return userMapper.toUserDTO(updatedUser);
    }

    @Override
    @Transactional
    public void deleteProfile(Long userId) {
        log.info("Eliminando perfil del usuario con ID: {}", userId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_EXCEPTION_MESSAGE + userId));

        // Eliminación lógica
        user.setDeleted(true);
        user.setEnabled(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("Perfil eliminado exitosamente para usuario con ID: {}", userId);
    }

    @Override
    @Transactional
    public UserDTO convertToHost(Long userId) {
        log.info("Convirtiendo usuario {} a HOST", userId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_EXCEPTION_MESSAGE + userId));

        // Si ya tiene perfil de host, solo asegurar rol HOST y devolver
        if (user.getHostProfile() != null) {
            if (!user.hasRole(UserRole.HOST)) {
                user.addRole(UserRole.HOST);
                user.setUpdatedAt(LocalDateTime.now());
                user = userRepository.save(user);
            }
            return userMapper.toUserDTO(user);
        }

        // Crear perfil de host y enlazar bidireccionalmente
        HostProfileEntity profile = new HostProfileEntity();
        profile.setHost(user);
        profile.setBusinessName(null); // opcional, se puede completar luego
        profile.setVerified(false);

        // Sincronizar lado inverso también
        user.setHostProfile(profile);
        // Agregar rol HOST manteniendo los roles existentes
        user.addRole(UserRole.HOST);
        user.setUpdatedAt(LocalDateTime.now());

        // Persistir perfil (lado propietario) primero para asegurar FK
        hostProfileRepository.save(profile);
        // Guardar usuario para actualizar campos de auditoría
        UserEntity updatedUser = userRepository.save(user);

        log.info("Usuario {} convertido exitosamente a HOST y perfil creado con ID: {}", userId, profile.getId());

        return userMapper.toUserDTO(updatedUser);
    }

    @Override
    @Transactional
    public String uploadProfileImage(Long userId, MultipartFile imageFile) throws UploadingStorageException {
        log.info("Subiendo foto de perfil para usuario: {}", userId);

        if (imageFile == null || imageFile.isEmpty()) {
            throw new UploadingStorageException("El archivo de imagen es requerido");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_EXCEPTION_MESSAGE + userId));

        if (Boolean.TRUE.equals(user.getDeleted())) {
            throw new IllegalStateException("No se puede subir imagen a un usuario eliminado");
        }

        try {
            // Si ya tiene una foto de perfil, eliminarla de Cloudinary
            if (user.getProfileImage() != null) {
                deleteExistingProfileImage(user);
            }

            // Subir nueva imagen a Cloudinary
            Map<Object, Object> uploadResult = storageService.upload(imageFile);
            String imageUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");
            String thumbnailUrl = (String) uploadResult.get("eager");

            // Crear nueva entidad de imagen
            ImageEntity profileImage = createProfileImageEntity(imageUrl, publicId, thumbnailUrl, user);

            // Guardar la imagen y actualizar el usuario
            imageRepository.save(profileImage);
            user.setProfileImage(profileImage);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);

            log.info("Foto de perfil subida exitosamente para usuario {}: {}", userId, imageUrl);
            return imageUrl;

        } catch (UploadingStorageException | DeletingStorageException e) {
            log.error("Error subiendo foto de perfil para usuario {}: {}", userId, e.getMessage());
            throw new UploadingStorageException("Error subiendo foto de perfil: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteProfileImage(Long userId) throws DeletingStorageException {
        log.info("Eliminando foto de perfil para usuario: {}", userId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_EXCEPTION_MESSAGE + userId));

        if (user.getProfileImage() == null) {
            throw new DeletingStorageException("El usuario no tiene foto de perfil");
        }

        try {
            ImageEntity profileImage = user.getProfileImage();

            // Eliminar de Cloudinary si tiene publicId
            if (profileImage.getCloudinaryPublicId() != null) {
                storageService.delete(profileImage.getCloudinaryPublicId());
            }

            // Eliminar de la base de datos
            user.setProfileImage(null);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            imageRepository.delete(profileImage);

            log.info("Foto de perfil eliminada exitosamente para usuario: {}", userId);

        } catch (DeletingStorageException e) {
            log.error("Error eliminando foto de perfil para usuario {}: {}", userId, e.getMessage());
            throw e;
        }
    }

    // Métodos privados auxiliares
    private void deleteExistingProfileImage(UserEntity user) throws DeletingStorageException {
        ImageEntity existingImage = user.getProfileImage();
        if (existingImage != null && existingImage.getCloudinaryPublicId() != null) {
            storageService.delete(existingImage.getCloudinaryPublicId());
        }
        if (existingImage != null) {
            imageRepository.delete(existingImage);
        }
    }

    private ImageEntity createProfileImageEntity(String url, String publicId, String thumbnailUrl, UserEntity user) {
        ImageEntity image = new ImageEntity();
        image.setUrl(url);
        image.setCloudinaryPublicId(publicId);
        image.setCloudinaryThumbnailUrl(thumbnailUrl);
        image.setIsPrimary(true); // La foto de perfil siempre es primaria
        image.setCreatedAt(LocalDateTime.now());
        image.setUser(user); // Establecer la relación bidireccional
        return image;
    }

}
