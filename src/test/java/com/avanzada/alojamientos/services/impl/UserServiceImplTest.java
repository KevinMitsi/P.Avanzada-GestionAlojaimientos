package com.avanzada.alojamientos.services.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
import com.avanzada.alojamientos.services.StorageService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private HostProfileRepository hostProfileRepository;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private StorageService storageService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UserEntity userEntity;
    private UserDTO userDTO;
    private RegisterUserDTO registerUserDTO;
    private CreateUserDTO createUserDTO;
    private EditUserDTO editUserDTO;

    @BeforeEach
    void setUp() {
        // Arrange - Setup common test data
        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("test@example.com");
        userEntity.setPassword("encodedPassword");
        userEntity.setName("Test User");
        userEntity.setDescription("Test description");
        userEntity.setRoles(Set.of(UserRole.USER));
        userEntity.setEnabled(true);
        userEntity.setDeleted(false);
        userEntity.setVerified(false);
        userEntity.setCreatedAt(LocalDateTime.now());

        userDTO = new UserDTO(
            1L,
            "test@example.com",
            "Test User",
            "Test description",
                LocalDate.now(),
            Set.of(UserRole.USER),
            "asdasdaodiwaj.com",
            null,
                List.of(),
            null,
            null,
            null,
                null,
                false
        );

        registerUserDTO = new RegisterUserDTO(
            "test@example.com",
            "Test User",
            "password123",
            "3001234567",
            LocalDate.of(1990, 1, 1)
        );

        createUserDTO = new CreateUserDTO(
                "Test User",
                "test@example.com",
            "password123",
            "Testpassword",
                LocalDate.of(1990, 1, 1)

        );

        editUserDTO = new EditUserDTO(
            "Updated Name",
            "3007654321",
            LocalDate.of(1991, 2, 2),
            "Updated description"
        );
    }

    @Test
    void register_ShouldCreateUserSuccessfully_WhenValidData() {
        // Arrange
        when(userRepository.existsByEmail(registerUserDTO.email())).thenReturn(false);
        when(userMapper.toEntity(registerUserDTO)).thenReturn(userEntity);
        when(passwordEncoder.encode(registerUserDTO.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
        when(userMapper.toUserDTO(userEntity)).thenReturn(userDTO);

        // Act
        UserDTO result = userService.register(registerUserDTO);

        // Assert
        assertNotNull(result);
        assertEquals(userDTO.email(), result.email());
        verify(userRepository).existsByEmail(registerUserDTO.email());
        verify(userMapper).toEntity(registerUserDTO);
        verify(passwordEncoder).encode(registerUserDTO.password());
        verify(userRepository).save(any(UserEntity.class));
        verify(userMapper).toUserDTO(userEntity);
    }

    @Test
    void register_ShouldThrowException_WhenEmailAlreadyExists() {
        // Arrange
        when(userRepository.existsByEmail(registerUserDTO.email())).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.register(registerUserDTO)
        );
        assertEquals("El email ya está registrado", exception.getMessage());
        verify(userRepository).existsByEmail(registerUserDTO.email());
        verifyNoMoreInteractions(userMapper, passwordEncoder, userRepository);
    }

    @Test
    void register_ShouldSetDefaultRole_WhenRolesAreNull() {
        // Arrange
        UserEntity userWithoutRoles = new UserEntity();
        userWithoutRoles.setEmail("test@example.com");
        userWithoutRoles.setRoles(null);

        when(userRepository.existsByEmail(registerUserDTO.email())).thenReturn(false);
        when(userMapper.toEntity(registerUserDTO)).thenReturn(userWithoutRoles);
        when(passwordEncoder.encode(registerUserDTO.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
        when(userMapper.toUserDTO(userEntity)).thenReturn(userDTO);

        // Act
        UserDTO result = userService.register(registerUserDTO);

        // Assert
        assertNotNull(result);
        verify(userRepository).save(argThat(user -> user.getRoles().contains(UserRole.USER)));
    }

    @Test
    void register_ShouldSetDefaultDescription_WhenDescriptionIsEmpty() {
        // Arrange
        RegisterUserDTO dtoWithEmptyDescription = new RegisterUserDTO(
            "test@example.com",
            "Test User",
            "password123",
            "3001234567",
            LocalDate.of(1990, 1, 1)
        );

        UserEntity userWithEmptyDescription = new UserEntity();
        userWithEmptyDescription.setEmail("test@example.com");
        userWithEmptyDescription.setDescription("   ");

        when(userRepository.existsByEmail(dtoWithEmptyDescription.email())).thenReturn(false);
        when(userMapper.toEntity(dtoWithEmptyDescription)).thenReturn(userWithEmptyDescription);
        when(passwordEncoder.encode(dtoWithEmptyDescription.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
        when(userMapper.toUserDTO(userEntity)).thenReturn(userDTO);

        // Act
        UserDTO result = userService.register(dtoWithEmptyDescription);

        // Assert
        assertNotNull(result);
        verify(userRepository).save(argThat(user ->
            "Usuario registrado en la plataforma".equals(user.getDescription())
        ));
    }

    @Test
    void create_ShouldCreateUserSuccessfully_WhenValidData() {
        // Arrange
        when(userRepository.existsByEmail(createUserDTO.email())).thenReturn(false);
        when(userMapper.toEntity(createUserDTO)).thenReturn(userEntity);
        when(passwordEncoder.encode(createUserDTO.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
        when(userMapper.toUserDTO(userEntity)).thenReturn(userDTO);

        // Act
        UserDTO result = userService.create(createUserDTO);

        // Assert
        assertNotNull(result);
        assertEquals(userDTO.email(), result.email());
        verify(userRepository).existsByEmail(createUserDTO.email());
        verify(userMapper).toEntity(createUserDTO);
        verify(passwordEncoder).encode(createUserDTO.password());
        verify(userRepository).save(any(UserEntity.class));
        verify(userMapper).toUserDTO(userEntity);
    }

    @Test
    void create_ShouldThrowException_WhenEmailAlreadyExists() {
        // Arrange
        when(userRepository.existsByEmail(createUserDTO.email())).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.create(createUserDTO)
        );
        assertEquals("El email ya está registrado", exception.getMessage());
        verify(userRepository).existsByEmail(createUserDTO.email());
        verifyNoMoreInteractions(userMapper, passwordEncoder, userRepository);
    }

    @Test
    void findById_ShouldReturnUser_WhenUserExistsAndNotDeleted() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userMapper.toUserDTO(userEntity)).thenReturn(userDTO);

        // Act
        Optional<UserDTO> result = userService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(userDTO.email(), result.get().email());
        verify(userRepository).findById(1L);
        verify(userMapper).toUserDTO(userEntity);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenUserIsDeleted() {
        // Arrange
        userEntity.setDeleted(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));

        // Act
        Optional<UserDTO> result = userService.findById(1L);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository).findById(1L);
        verifyNoInteractions(userMapper);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        Optional<UserDTO> result = userService.findById(1L);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository).findById(1L);
        verifyNoInteractions(userMapper);
    }

    @Test
    void enable_ShouldEnableUser_WhenUserExists() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        // Act
        userService.enable("1", true);

        // Assert
        verify(userRepository).findById(1L);
        verify(userRepository).save(argThat(UserEntity::getEnabled));
    }

    @Test
    void enable_ShouldDisableUser_WhenUserExists() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        // Act
        userService.enable("1", false);

        // Assert
        verify(userRepository).findById(1L);
        verify(userRepository).save(argThat(user -> !user.getEnabled()));
    }

    @Test
    void enable_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(
            UserNotFoundException.class,
            () -> userService.enable("1", true)
        );
        assertEquals("Usuario no encontrado con ID: 1", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_ShouldUpdatePassword_WhenOldPasswordIsCorrect() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches("oldPassword", userEntity.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        // Act
        userService.changePassword(1L, "oldPassword", "newPassword");

        // Assert
        verify(userRepository).findById(1L);
        verify(passwordEncoder).matches("oldPassword", userEntity.getPassword());
        verify(passwordEncoder).encode("newPassword");
        verify(userRepository).save(argThat(user -> "newEncodedPassword".equals(user.getPassword())));
    }

    @Test
    void changePassword_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(
            UserNotFoundException.class,
            () -> userService.changePassword(1L, "oldPassword", "newPassword")
        );
        assertEquals("Usuario no encontrado con ID: 1", exception.getMessage());
        verify(userRepository).findById(1L);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void changePassword_ShouldThrowException_WhenOldPasswordIsIncorrect() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches("wrongPassword", userEntity.getPassword())).thenReturn(false);

        // Act & Assert
        InvalidPasswordException exception = assertThrows(
            InvalidPasswordException.class,
            () -> userService.changePassword(1L, "wrongPassword", "newPassword")
        );
        assertEquals("La contraseña actual no es correcta", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(passwordEncoder).matches("wrongPassword", userEntity.getPassword());
        verify(passwordEncoder, never()).encode("newPassword");
        verify(userRepository, never()).save(any());
    }

    @Test
    void editProfile_ShouldUpdateProfile_WhenUserExists() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
        when(userMapper.toUserDTO(userEntity)).thenReturn(userDTO);

        // Act
        UserDTO result = userService.editProfile(1L, editUserDTO);

        // Assert
        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(userMapper).updateEntityFromDTO(editUserDTO, userEntity);
        verify(userRepository).save(any(UserEntity.class));
        verify(userMapper).toUserDTO(userEntity);
    }

    @Test
    void editProfile_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(
            UserNotFoundException.class,
            () -> userService.editProfile(1L, editUserDTO)
        );
        assertEquals("Usuario no encontrado con ID: 1", exception.getMessage());
        verify(userRepository).findById(1L);
        verifyNoInteractions(userMapper);
    }

    @Test
    void deleteProfile_ShouldMarkAsDeleted_WhenUserExists() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        // Act
        userService.deleteProfile(1L);

        // Assert
        verify(userRepository).findById(1L);
        verify(userRepository).save(argThat(user ->
            user.getDeleted() && !user.getEnabled()
        ));
    }

    @Test
    void deleteProfile_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(
            UserNotFoundException.class,
            () -> userService.deleteProfile(1L)
        );
        assertEquals("Usuario no encontrado con ID: 1", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void convertToHost_ShouldCreateHostProfile_WhenUserHasNoHostProfile() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(hostProfileRepository.save(any(HostProfileEntity.class))).thenReturn(new HostProfileEntity());
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
        when(userMapper.toUserDTO(userEntity)).thenReturn(userDTO);

        // Act
        UserDTO result = userService.convertToHost(1L);

        // Assert
        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(hostProfileRepository).save(any(HostProfileEntity.class));
        verify(userRepository).save(argThat(user -> user.getRoles().contains(UserRole.HOST)));
        verify(userMapper).toUserDTO(userEntity);
    }

    @Test
    void convertToHost_ShouldAddHostRole_WhenUserHasHostProfileButNoHostRole() {
        // Arrange
        HostProfileEntity existingProfile = new HostProfileEntity();
        userEntity.setHostProfile(existingProfile);
        userEntity.setRoles(Set.of(UserRole.USER)); // No tiene rol HOST

        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
        when(userMapper.toUserDTO(userEntity)).thenReturn(userDTO);

        // Act
        UserDTO result = userService.convertToHost(1L);

        // Assert
        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(UserEntity.class));
        verify(userMapper).toUserDTO(userEntity);
        verifyNoInteractions(hostProfileRepository);
    }

    @Test
    void convertToHost_ShouldReturnUser_WhenUserAlreadyHasHostProfileAndRole() {
        // Arrange
        HostProfileEntity existingProfile = new HostProfileEntity();
        userEntity.setHostProfile(existingProfile);
        userEntity.setRoles(Set.of(UserRole.USER, UserRole.HOST));

        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userMapper.toUserDTO(userEntity)).thenReturn(userDTO);

        // Act
        UserDTO result = userService.convertToHost(1L);

        // Assert
        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(userMapper).toUserDTO(userEntity);
        verifyNoInteractions(hostProfileRepository, userRepository);
    }

    @Test
    void convertToHost_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(
            UserNotFoundException.class,
            () -> userService.convertToHost(1L)
        );
        assertEquals("Usuario no encontrado con ID: 1", exception.getMessage());
        verify(userRepository).findById(1L);
        verifyNoInteractions(hostProfileRepository, userMapper);
    }

    @Test
    void uploadProfileImage_ShouldUploadImage_WhenValidFile() {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));

        Map<Object, Object> uploadResult = Map.of(
            "secure_url", "https://example.com/image.jpg",
            "public_id", "public123",
            "eager", "https://example.com/thumbnail.jpg"
        );
        when(storageService.upload(mockFile)).thenReturn(uploadResult);
        when(imageRepository.save(any(ImageEntity.class))).thenReturn(new ImageEntity());
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        // Act
        String result = userService.uploadProfileImage(1L, mockFile);

        // Assert
        assertEquals("https://example.com/image.jpg", result);
        verify(userRepository).findById(1L);
        verify(storageService).upload(mockFile);
        verify(imageRepository).save(any(ImageEntity.class));
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void uploadProfileImage_ShouldDeleteExistingImage_WhenUserHasProfileImage() {
        // Arrange
        ImageEntity existingImage = new ImageEntity();
        existingImage.setCloudinaryPublicId("existing123");
        userEntity.setProfileImage(existingImage);

        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));

        Map<Object, Object> uploadResult = Map.of(
            "secure_url", "https://example.com/image.jpg",
            "public_id", "public123",
            "eager", "https://example.com/thumbnail.jpg"
        );
        when(storageService.upload(mockFile)).thenReturn(uploadResult);
        when(imageRepository.save(any(ImageEntity.class))).thenReturn(new ImageEntity());
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        // Act
        String result = userService.uploadProfileImage(1L, mockFile);

        // Assert
        assertEquals("https://example.com/image.jpg", result);
        verify(storageService).delete("existing123");
        verify(imageRepository).delete(existingImage);
    }

    @Test
    void uploadProfileImage_ShouldThrowException_WhenFileIsNull() {
        // Act & Assert
        UploadingStorageException exception = assertThrows(
            UploadingStorageException.class,
            () -> userService.uploadProfileImage(1L, null)
        );
        assertEquals("El archivo de imagen es requerido", exception.getMessage());
        verifyNoInteractions(userRepository, storageService, imageRepository);
    }

    @Test
    void uploadProfileImage_ShouldThrowException_WhenFileIsEmpty() {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(true);

        // Act & Assert
        UploadingStorageException exception = assertThrows(
            UploadingStorageException.class,
            () -> userService.uploadProfileImage(1L, mockFile)
        );
        assertEquals("El archivo de imagen es requerido", exception.getMessage());
        verifyNoInteractions(userRepository, storageService, imageRepository);
    }

    @Test
    void uploadProfileImage_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(
            UserNotFoundException.class,
            () -> userService.uploadProfileImage(1L, mockFile)
        );
        assertEquals("Usuario no encontrado con ID: 1", exception.getMessage());
        verify(userRepository).findById(1L);
        verifyNoInteractions(storageService, imageRepository);
    }

    @Test
    void uploadProfileImage_ShouldThrowException_WhenUserIsDeleted() {
        // Arrange
        userEntity.setDeleted(true);
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> userService.uploadProfileImage(1L, mockFile)
        );
        assertEquals("No se puede subir imagen a un usuario eliminado", exception.getMessage());
        verify(userRepository).findById(1L);
        verifyNoInteractions(storageService, imageRepository);
    }

    @Test
    void uploadProfileImage_ShouldThrowException_WhenStorageServiceFails() {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(storageService.upload(mockFile)).thenThrow(new UploadingStorageException("Storage error"));

        // Act & Assert
        UploadingStorageException exception = assertThrows(
            UploadingStorageException.class,
            () -> userService.uploadProfileImage(1L, mockFile)
        );
        assertTrue(exception.getMessage().contains("Error subiendo foto de perfil"));
        verify(userRepository).findById(1L);
        verify(storageService).upload(mockFile);
        verifyNoInteractions(imageRepository);
    }

    @Test
    void deleteProfileImage_ShouldDeleteImage_WhenUserHasProfileImage() {
        // Arrange
        ImageEntity profileImage = new ImageEntity();
        profileImage.setCloudinaryPublicId("public123");
        userEntity.setProfileImage(profileImage);

        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        // Act
        userService.deleteProfileImage(1L);

        // Assert
        verify(userRepository).findById(1L);
        verify(storageService).delete("public123");
        verify(userRepository).save(argThat(user -> user.getProfileImage() == null));
        verify(imageRepository).delete(profileImage);
    }

    @Test
    void deleteProfileImage_ShouldDeleteImageWithoutCloudinary_WhenNoPublicId() {
        // Arrange
        ImageEntity profileImage = new ImageEntity();
        profileImage.setCloudinaryPublicId(null);
        userEntity.setProfileImage(profileImage);

        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        // Act
        userService.deleteProfileImage(1L);

        // Assert
        verify(userRepository).findById(1L);
        verify(userRepository).save(argThat(user -> user.getProfileImage() == null));
        verify(imageRepository).delete(profileImage);
        verifyNoInteractions(storageService);
    }

    @Test
    void deleteProfileImage_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(
            UserNotFoundException.class,
            () -> userService.deleteProfileImage(1L)
        );
        assertEquals("Usuario no encontrado con ID: 1", exception.getMessage());
        verify(userRepository).findById(1L);
        verifyNoInteractions(storageService, imageRepository);
    }

    @Test
    void deleteProfileImage_ShouldThrowException_WhenUserHasNoProfileImage() {
        // Arrange
        userEntity.setProfileImage(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));

        // Act & Assert
        DeletingStorageException exception = assertThrows(
            DeletingStorageException.class,
            () -> userService.deleteProfileImage(1L)
        );
        assertEquals("El usuario no tiene foto de perfil", exception.getMessage());
        verify(userRepository).findById(1L);
        verifyNoInteractions(storageService, imageRepository);
    }

    @Test
    void deleteProfileImage_ShouldThrowException_WhenStorageServiceFails() {
        // Arrange
        ImageEntity profileImage = new ImageEntity();
        profileImage.setCloudinaryPublicId("public123");
        userEntity.setProfileImage(profileImage);

        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        doThrow(new DeletingStorageException("Storage deletion error"))
            .when(storageService).delete("public123");

        // Act & Assert
        DeletingStorageException exception = assertThrows(
            DeletingStorageException.class,
            () -> userService.deleteProfileImage(1L)
        );
        assertEquals("Storage deletion error", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(storageService).delete("public123");
        verifyNoMoreInteractions(userRepository, imageRepository);
    }
}