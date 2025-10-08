package com.avanzada.alojamientos.services;

import com.avanzada.alojamientos.DTO.user.CreateUserDTO;
import com.avanzada.alojamientos.DTO.user.EditUserDTO;
import com.avanzada.alojamientos.DTO.auth.RegisterUserDTO;
import com.avanzada.alojamientos.DTO.user.UserDTO;
import com.avanzada.alojamientos.exceptions.DeletingStorageException;
import com.avanzada.alojamientos.exceptions.UploadingStorageException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface UserService {
    UserDTO register(RegisterUserDTO dto);
    UserDTO create(CreateUserDTO dto);
    UserDTO edit(Long userId, EditUserDTO dto);
    Optional<UserDTO> findById(Long id);
    void enable(String userId, boolean enable);
    void delete(String userId);
    void changePassword(String userId, String oldPassword, String newPassword);

    // Nuevos métodos para la funcionalidad de HOST
    UserDTO convertToHost(Long userId);

    // Métodos para manejo de foto de perfil
    String uploadProfileImage(Long userId, MultipartFile imageFile) throws UploadingStorageException;
    void deleteProfileImage(Long userId) throws DeletingStorageException;
}
