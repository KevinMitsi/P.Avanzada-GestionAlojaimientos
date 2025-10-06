package com.avanzada.alojamientos.services.impl;

import com.avanzada.alojamientos.DTO.user.CreateUserDTO;
import com.avanzada.alojamientos.DTO.user.EditUserDTO;
import com.avanzada.alojamientos.DTO.auth.RegisterUserDTO;
import com.avanzada.alojamientos.DTO.user.UserDTO;
import com.avanzada.alojamientos.DTO.model.UserRole;
import com.avanzada.alojamientos.entities.HostProfileEntity;
import com.avanzada.alojamientos.entities.UserEntity;
import com.avanzada.alojamientos.exceptions.UserNotFoundException;
import com.avanzada.alojamientos.exceptions.InvalidPasswordException;
import com.avanzada.alojamientos.repositories.UserRepository;
import com.avanzada.alojamientos.repositories.HostProfileRepository;
import com.avanzada.alojamientos.mappers.UserMapper;
import com.avanzada.alojamientos.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final HostProfileRepository hostProfileRepository;
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
    @Transactional
    public UserDTO edit(Long userId, EditUserDTO dto) {
        log.info("Editando usuario con ID: {}", userId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + userId));

        // Actualizar campos desde DTO
        userMapper.updateEntityFromDTO(dto, user);
        user.setUpdatedAt(LocalDateTime.now());

        // Guardar cambios
        UserEntity updatedUser = userRepository.save(user);
        log.info("Usuario actualizado exitosamente con ID: {}", updatedUser.getId());

        return userMapper.toUserDTO(updatedUser);
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
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + userId));

        user.setEnabled(enable);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("Estado del usuario {} cambiado a: {}", userId, enable);
    }

    @Override
    @Transactional
    public void delete(String userId) {
        log.info("Eliminando usuario con ID: {}", userId);

        Long id = Long.parseLong(userId);
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + userId));

        // Eliminación lógica
        user.setDeleted(true);
        user.setEnabled(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("Usuario eliminado exitosamente con ID: {}", userId);
    }

    @Override
    @Transactional
    public void changePassword(String userId, String oldPassword, String newPassword) {
        log.info("Cambiando contraseña para usuario: {}", userId);

        Long id = Long.parseLong(userId);
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + userId));

        // Verificar contraseña actual
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new InvalidPasswordException("La contraseña actual no es correcta");
        }

        // Actualizar contraseña
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("Contraseña actualizada exitosamente para usuario: {}", userId);
    }

    @Override
    @Transactional
    public UserDTO convertToHost(Long userId) {
        log.info("Convirtiendo usuario {} a HOST", userId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + userId));

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
    @Transactional(readOnly = true)
    public UserEntity findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con email: " + email));
    }
}
