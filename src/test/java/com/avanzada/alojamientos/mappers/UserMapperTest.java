package com.avanzada.alojamientos.mappers;

import com.avanzada.alojamientos.DTO.auth.RegisterUserDTO;
import com.avanzada.alojamientos.DTO.model.UserRole;
import com.avanzada.alojamientos.DTO.user.CreateUserDTO;
import com.avanzada.alojamientos.DTO.user.EditUserDTO;
import com.avanzada.alojamientos.DTO.user.UserDTO;
import com.avanzada.alojamientos.entities.ImageEntity;
import com.avanzada.alojamientos.entities.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    private UserEntity userEntity;
    private CreateUserDTO createUserDTO;
    private RegisterUserDTO registerUserDTO;
    private EditUserDTO editUserDTO;
    private ImageEntity profileImage;

    @BeforeEach
    void setUp() {
        // Setup Profile Image
        profileImage = new ImageEntity();
        profileImage.setId(1L);
        profileImage.setUrl("https://example.com/profile.jpg");
        profileImage.setCreatedAt(LocalDateTime.now());

        // Setup User Entity
        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setName("Juan Pérez");
        userEntity.setEmail("juan@example.com");
        userEntity.setPhone("3001234567");
        userEntity.setPassword("hashedPassword123");
        userEntity.setDateOfBirth(LocalDate.of(1990, 5, 15));
        userEntity.setDescription("Soy un viajero entusiasta");
        userEntity.setProfileImage(profileImage);
        userEntity.setDocumentsUrl(List.of("https://docs.com/doc1.pdf"));
        userEntity.setRoles(Set.of(UserRole.USER));
        userEntity.setVerified(true);
        userEntity.setEnabled(true);
        userEntity.setDeleted(false);
        userEntity.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
        userEntity.setUpdatedAt(LocalDateTime.of(2025, 6, 15, 14, 30));

        // Setup CreateUserDTO
        createUserDTO = new CreateUserDTO(
                "María García",
                "3009876543",
                "maria@example.com",
                "password123",
                LocalDate.of(1995, 8, 20)
        );

        // Setup RegisterUserDTO
        registerUserDTO = new RegisterUserDTO(
                "pedro@example.com",
                "password456",
                "Pedro Martínez",
                "3005551234",
                LocalDate.of(1988, 3, 10)
        );

        // Setup EditUserDTO
        editUserDTO = new EditUserDTO(
                "Juan Pérez Actualizado",
                "3001111111",
                LocalDate.of(1990, 5, 16),
                "Descripción actualizada"
        );
    }

    @Test
    @DisplayName("Debe mapear UserEntity a UserDTO correctamente")
    void toUserDTO_shouldMapCorrectly() {
        // When
        UserDTO dto = userMapper.toUserDTO(userEntity);

        // Then
        assertNotNull(dto);
        assertEquals(userEntity.getId(), dto.id());
        assertEquals(userEntity.getName(), dto.name());
        assertEquals(userEntity.getEmail(), dto.email());
        assertEquals(userEntity.getPhone(), dto.phone());
        assertEquals(userEntity.getDateOfBirth(), dto.dateOfBirth());
        assertEquals(userEntity.getRoles(), dto.roles());
        assertEquals(userEntity.getDescription(), dto.description());
        assertEquals(userEntity.getDocumentsUrl(), dto.documentsUrl());
        assertEquals(userEntity.getVerified(), dto.verified());
        assertEquals(userEntity.getEnabled(), dto.enabled());
        assertEquals(userEntity.getDeleted(), dto.deleted());

        // Verificar profileImageUrl desde profileImage.url
        assertEquals(profileImage.getUrl(), dto.profileImageUrl());

        // Verificar formato de fechas
        assertEquals("2025-01-01T10:00:00", dto.createdAt());
        assertEquals("2025-06-15T14:30:00", dto.updatedAt());
    }

    @Test
    @DisplayName("Debe retornar null cuando UserEntity es null")
    void toUserDTO_withNullEntity_shouldReturnNull() {
        // When
        UserDTO dto = userMapper.toUserDTO(null);

        // Then
        assertNull(dto);
    }

    @Test
    @DisplayName("Debe manejar profileImage null correctamente")
    void toUserDTO_withNullProfileImage_shouldMapCorrectly() {
        // Given
        userEntity.setProfileImage(null);

        // When
        UserDTO dto = userMapper.toUserDTO(userEntity);

        // Then
        assertNotNull(dto);
        assertNull(dto.profileImageUrl());
    }

    @Test
    @DisplayName("Debe mapear CreateUserDTO a UserEntity correctamente")
    void toEntity_fromCreateUserDTO_shouldMapCorrectly() {
        // When
        UserEntity entity = userMapper.toEntity(createUserDTO);

        // Then
        assertNotNull(entity);
        assertEquals(createUserDTO.name(), entity.getName());
        assertEquals(createUserDTO.email(), entity.getEmail());
        assertEquals(createUserDTO.phone(), entity.getPhone());
        assertEquals(createUserDTO.password(), entity.getPassword());
        assertEquals(createUserDTO.dateBirth(), entity.getDateOfBirth());

        // Verificar valores por defecto
        assertNull(entity.getId());
        assertFalse(entity.getVerified());
        assertTrue(entity.getEnabled());
        assertFalse(entity.getDeleted());
        assertNotNull(entity.getCreatedAt());

        // Verificar campos ignorados
        assertNull(entity.getUpdatedAt());
        assertEquals(new ArrayList<>() , entity.getAccommodations());
        assertEquals(new ArrayList<>() , entity.getReservations());
        assertEquals(new ArrayList<>(),entity.getComments());
        assertEquals(new ArrayList<>(),entity.getFavorites());
        assertEquals(new ArrayList<>(),entity.getNotifications());
        assertNull(entity.getHostProfile());
        assertNull(entity.getProfileImage());
        assertNull(entity.getDescription());
        assertEquals(new ArrayList<>(),entity.getDocumentsUrl());
        assertEquals(new HashSet<>(),entity.getRoles());
    }

    @Test
    @DisplayName("Debe retornar null cuando CreateUserDTO es null")
    void toEntity_fromCreateUserDTO_withNull_shouldReturnNull() {
        // When
        UserEntity entity = userMapper.toEntity((CreateUserDTO) null);

        // Then
        assertNull(entity);
    }

    @Test
    @DisplayName("Debe mapear RegisterUserDTO a UserEntity correctamente")
    void toEntity_fromRegisterUserDTO_shouldMapCorrectly() {
        // When
        UserEntity entity = userMapper.toEntity(registerUserDTO);

        // Then
        assertNotNull(entity);
        assertEquals(registerUserDTO.name(), entity.getName());
        assertEquals(registerUserDTO.email(), entity.getEmail());
        assertEquals(registerUserDTO.phone(), entity.getPhone());
        assertEquals(registerUserDTO.password(), entity.getPassword());
        assertEquals(registerUserDTO.dateOfBirth(), entity.getDateOfBirth());

        // Verificar valores por defecto
        assertNull(entity.getId());
        assertFalse(entity.getVerified());
        assertTrue(entity.getEnabled());
        assertFalse(entity.getDeleted());
        assertNotNull(entity.getCreatedAt());

        // Verificar campos ignorados
        assertNull(entity.getUpdatedAt());
        assertEquals(new HashSet<>(),entity.getRoles());
    }

    @Test
    @DisplayName("Debe retornar null cuando RegisterUserDTO es null")
    void toEntity_fromRegisterUserDTO_withNull_shouldReturnNull() {
        // When
        UserEntity entity = userMapper.toEntity((RegisterUserDTO) null);

        // Then
        assertNull(entity);
    }

    @Test
    @DisplayName("Debe generar createdAt automáticamente al crear entidad")
    void toEntity_shouldGenerateCreatedAt() {
        // Given
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        // When
        UserEntity entity = userMapper.toEntity(createUserDTO);

        // Then
        assertNotNull(entity.getCreatedAt());
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);
        assertTrue(entity.getCreatedAt().isAfter(before));
        assertTrue(entity.getCreatedAt().isBefore(after));
    }

    @Test
    @DisplayName("Debe actualizar entidad desde EditUserDTO correctamente")
    void updateEntityFromDTO_shouldUpdateOnlyProvidedFields() {
        // Given
        String originalEmail = userEntity.getEmail();
        String originalPassword = userEntity.getPassword();
        LocalDateTime originalCreatedAt = userEntity.getCreatedAt();

        // When
        userMapper.updateEntityFromDTO(editUserDTO, userEntity);

        // Then
        // Campos actualizados
        assertEquals(editUserDTO.name(), userEntity.getName());
        assertEquals(editUserDTO.phone(), userEntity.getPhone());
        assertEquals(editUserDTO.dateBirth(), userEntity.getDateOfBirth());
        assertEquals(editUserDTO.description(), userEntity.getDescription());
        assertNotNull(userEntity.getUpdatedAt());

        // Campos que NO deben cambiar (ignorados)
        assertEquals(originalEmail, userEntity.getEmail());
        assertEquals(originalPassword, userEntity.getPassword());
        assertEquals(originalCreatedAt, userEntity.getCreatedAt());
        assertEquals(1L, userEntity.getId());
        assertTrue(userEntity.getVerified());
        assertTrue(userEntity.getEnabled());
        assertFalse(userEntity.getDeleted());
    }

    @Test
    @DisplayName("Debe ignorar valores null en EditUserDTO (NullValuePropertyMappingStrategy.IGNORE)")
    void updateEntityFromDTO_withNullValues_shouldNotUpdateNullFields() {
        // Given
        EditUserDTO partialDTO = new EditUserDTO(
                "Solo nombre actualizado",
                null, // phone null
                null, // dateBirth null
                null  // description null
        );

        String originalPhone = userEntity.getPhone();
        LocalDate originalDateOfBirth = userEntity.getDateOfBirth();
        String originalDescription = userEntity.getDescription();

        // When
        userMapper.updateEntityFromDTO(partialDTO, userEntity);

        // Then
        assertEquals("Solo nombre actualizado", userEntity.getName());

        // Estos NO deben cambiar porque son null en el DTO
        assertEquals(originalPhone, userEntity.getPhone());
        assertEquals(originalDateOfBirth, userEntity.getDateOfBirth());
        assertEquals(originalDescription, userEntity.getDescription());
    }

    @Test
    @DisplayName("Debe mapear dateBirth a dateOfBirth correctamente en actualización")
    void updateEntityFromDTO_shouldMapDateBirthToDateOfBirth() {
        // Given
        LocalDate newDateOfBirth = LocalDate.of(1991, 1, 1);
        EditUserDTO dto = new EditUserDTO(null, null, newDateOfBirth, null);

        // When
        userMapper.updateEntityFromDTO(dto, userEntity);

        // Then
        assertEquals(newDateOfBirth, userEntity.getDateOfBirth());
    }

    @Test
    @DisplayName("Debe manejar diferentes roles correctamente")
    void toUserDTO_withDifferentRoles_shouldMapCorrectly() {
        // Given - Solo USER
        userEntity.setRoles(Set.of(UserRole.USER));
        UserDTO dto1 = userMapper.toUserDTO(userEntity);
        assertEquals(1, dto1.roles().size());
        assertTrue(dto1.roles().contains(UserRole.USER));

        // Given - USER y HOST
        userEntity.setRoles(Set.of(UserRole.USER, UserRole.HOST));
        UserDTO dto2 = userMapper.toUserDTO(userEntity);
        assertEquals(2, dto2.roles().size());
        assertTrue(dto2.roles().contains(UserRole.USER));
        assertTrue(dto2.roles().contains(UserRole.HOST));
    }

    @Test
    @DisplayName("Debe manejar usuario no verificado correctamente")
    void toUserDTO_withUnverifiedUser_shouldMapCorrectly() {
        // Given
        userEntity.setVerified(false);
        userEntity.setEnabled(false);

        // When
        UserDTO dto = userMapper.toUserDTO(userEntity);

        // Then
        assertNotNull(dto);
        assertFalse(dto.verified());
        assertFalse(dto.enabled());
    }

    @Test
    @DisplayName("Debe manejar usuario eliminado correctamente")
    void toUserDTO_withDeletedUser_shouldMapCorrectly() {
        // Given
        userEntity.setDeleted(true);

        // When
        UserDTO dto = userMapper.toUserDTO(userEntity);

        // Then
        assertNotNull(dto);
        assertTrue(dto.deleted());
    }

    @Test
    @DisplayName("Debe manejar múltiples documentos URL correctamente")
    void toUserDTO_withMultipleDocuments_shouldMapCorrectly() {
        // Given
        List<String> documents = List.of(
                "https://docs.com/doc1.pdf",
                "https://docs.com/doc2.pdf",
                "https://docs.com/doc3.pdf"
        );
        userEntity.setDocumentsUrl(documents);

        // When
        UserDTO dto = userMapper.toUserDTO(userEntity);

        // Then
        assertNotNull(dto);
        assertNotNull(dto.documentsUrl());
        assertEquals(3, dto.documentsUrl().size());
        assertEquals(documents, dto.documentsUrl());
    }

    @Test
    @DisplayName("Debe manejar documentsUrl null correctamente")
    void toUserDTO_withNullDocuments_shouldMapCorrectly() {
        // Given
        userEntity.setDocumentsUrl(null);

        // When
        UserDTO dto = userMapper.toUserDTO(userEntity);

        // Then
        assertNotNull(dto);
        assertNull(dto.documentsUrl());
    }

    @Test
    @DisplayName("Debe mantener consistencia en múltiples llamadas con los mismos datos")
    void toUserDTO_shouldBeConsistentAcrossMultipleCalls() {
        // When
        UserDTO dto1 = userMapper.toUserDTO(userEntity);
        UserDTO dto2 = userMapper.toUserDTO(userEntity);

        // Then
        assertNotNull(dto1);
        assertNotNull(dto2);
        assertEquals(dto1.id(), dto2.id());
        assertEquals(dto1.name(), dto2.name());
        assertEquals(dto1.email(), dto2.email());
        assertEquals(dto1.profileImageUrl(), dto2.profileImageUrl());
        assertEquals(dto1.createdAt(), dto2.createdAt());
    }

    @Test
    @DisplayName("Debe establecer valores por defecto correctamente al crear desde CreateUserDTO")
    void toEntity_fromCreateUserDTO_shouldSetDefaultValues() {
        // When
        UserEntity entity = userMapper.toEntity(createUserDTO);

        // Then
        assertFalse(entity.getVerified(), "verified debe ser false por defecto");
        assertTrue(entity.getEnabled(), "enabled debe ser true por defecto");
        assertFalse(entity.getDeleted(), "deleted debe ser false por defecto");
    }

    @Test
    @DisplayName("Debe establecer valores por defecto correctamente al crear desde RegisterUserDTO")
    void toEntity_fromRegisterUserDTO_shouldSetDefaultValues() {
        // When
        UserEntity entity = userMapper.toEntity(registerUserDTO);

        // Then
        assertFalse(entity.getVerified(), "verified debe ser false por defecto");
        assertTrue(entity.getEnabled(), "enabled debe ser true por defecto");
        assertFalse(entity.getDeleted(), "deleted debe ser false por defecto");
    }

    @Test
    @DisplayName("Debe actualizar updatedAt automáticamente al editar")
    void updateEntityFromDTO_shouldUpdateUpdatedAt() {
        // Given
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        userEntity.setUpdatedAt(null);

        // When
        userMapper.updateEntityFromDTO(editUserDTO, userEntity);

        // Then
        assertNotNull(userEntity.getUpdatedAt());
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);
        assertTrue(userEntity.getUpdatedAt().isAfter(before));
        assertTrue(userEntity.getUpdatedAt().isBefore(after));
    }

    @Test
    @DisplayName("Debe manejar descripción larga correctamente")
    void updateEntityFromDTO_withLongDescription_shouldMapCorrectly() {
        // Given
        String longDescription = "a".repeat(500);
        EditUserDTO dto = new EditUserDTO(null, null, null, longDescription);

        // When
        userMapper.updateEntityFromDTO(dto, userEntity);

        // Then
        assertEquals(longDescription, userEntity.getDescription());
        assertEquals(500, userEntity.getDescription().length());
    }

    @Test
    @DisplayName("Debe manejar roles vacíos correctamente")
    void toUserDTO_withEmptyRoles_shouldMapCorrectly() {
        // Given
        userEntity.setRoles(new HashSet<>());

        // When
        UserDTO dto = userMapper.toUserDTO(userEntity);

        // Then
        assertNotNull(dto);
        assertNotNull(dto.roles());
        assertTrue(dto.roles().isEmpty());
    }
}