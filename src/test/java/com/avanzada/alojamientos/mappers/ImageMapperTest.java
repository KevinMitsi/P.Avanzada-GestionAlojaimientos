package com.avanzada.alojamientos.mappers;

import com.avanzada.alojamientos.DTO.other.ImageDTO;
import com.avanzada.alojamientos.entities.AccommodationEntity;
import com.avanzada.alojamientos.entities.ImageEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ImageMapperTest {

    @Autowired
    private ImageMapper imageMapper;

    private ImageEntity imageEntity;
    private ImageDTO imageDTO;

    @BeforeEach
    void setUp() {
        // Setup Accommodation Entity
        AccommodationEntity accommodationEntity = new AccommodationEntity();
        accommodationEntity.setId(100L);
        accommodationEntity.setTitle("Apartamento de prueba");

        // Setup Image Entity
        imageEntity = new ImageEntity();
        imageEntity.setId(1L);
        imageEntity.setUrl("https://example.com/images/photo1.jpg");
        imageEntity.setThumbnailUrl("https://example.com/images/photo1_thumb.jpg");
        imageEntity.setCloudinaryPublicId("cloudinary_public_id_123");
        imageEntity.setCloudinaryThumbnailUrl("https://res.cloudinary.com/thumb123.jpg");
        imageEntity.setIsPrimary(true);
        imageEntity.setCreatedAt(LocalDateTime.of(2025, 6, 10, 14, 30, 45));
        imageEntity.setAccommodation(accommodationEntity);

        // Setup Image DTO
        imageDTO = new ImageDTO(
                2L,
                "https://example.com/images/photo2.jpg",
                "https://example.com/images/photo2_thumb.jpg",
                false,
                LocalDateTime.of(2025, 7, 15, 10, 20, 30)
        );
    }

    @Test
    @DisplayName("Debe mapear ImageEntity a ImageDTO correctamente")
    void toModel_shouldMapCorrectly() {
        // When
        ImageDTO dto = imageMapper.toModel(imageEntity);

        // Then
        assertNotNull(dto);
        assertEquals(imageEntity.getId(), dto.id());
        assertEquals(imageEntity.getUrl(), dto.url());
        assertEquals(imageEntity.getThumbnailUrl(), dto.thumbnailUrl());
        assertEquals(imageEntity.getIsPrimary(), dto.isPrimary());

        // Verificar formato de fecha: "yyyy-MM-dd'T'HH:mm:ss"
        assertNotNull(dto.createdAt());
        assertEquals(LocalDateTime.of(2025, 6, 10, 14, 30, 45), dto.createdAt());
    }

    @Test
    @DisplayName("Debe retornar null cuando ImageEntity es null")
    void toModel_withNullEntity_shouldReturnNull() {
        // When
        ImageDTO dto = imageMapper.toModel(null);

        // Then
        assertNull(dto);
    }

    @Test
    @DisplayName("Debe formatear correctamente la fecha con formato ISO-8601")
    void toModel_shouldFormatDateCorrectly() {
        // Given - Diferentes fechas
        imageEntity.setCreatedAt(LocalDateTime.of(2025, 1, 1, 0, 0, 0));
        ImageDTO dto1 = imageMapper.toModel(imageEntity);
        assertEquals(LocalDateTime.of(2025, 1, 1, 0, 0, 0), dto1.createdAt());

        imageEntity.setCreatedAt(LocalDateTime.of(2025, 12, 31, 23, 59, 59));
        ImageDTO dto2 = imageMapper.toModel(imageEntity);
        assertEquals(LocalDateTime.of(2025, 12, 31, 23, 59, 59), dto2.createdAt());
    }

    @Test
    @DisplayName("Debe mapear ImageDTO a ImageEntity correctamente")
    void toEntity_shouldMapCorrectly() {
        // When
        ImageEntity entity = imageMapper.toEntity(imageDTO);

        // Then
        assertNotNull(entity);
        assertEquals(imageDTO.url(), entity.getUrl());
        assertEquals(imageDTO.thumbnailUrl(), entity.getThumbnailUrl());

        // Verificar campos ignorados
        assertNull(entity.getId()); // Se ignora
        assertNull(entity.getCreatedAt()); // Se ignora
        assertNull(entity.getAccommodation()); // Se ignora
        assertNull(entity.getCloudinaryPublicId()); // Se ignora
        assertNull(entity.getCloudinaryThumbnailUrl()); // Se ignora

        // Verificar valor por defecto
        assertFalse(entity.getIsPrimary()); // constant = "false"
    }

    @Test
    @DisplayName("Debe retornar null cuando ImageDTO es null")
    void toEntity_withNullDTO_shouldReturnNull() {
        // When
        ImageEntity entity = imageMapper.toEntity(null);

        // Then
        assertNull(entity);
    }

    @Test
    @DisplayName("Debe establecer isPrimary en false por defecto al crear entidad")
    void toEntity_shouldSetIsPrimaryToFalse() {
        // Given - DTO con isPrimary true
        ImageDTO dtoWithPrimaryTrue = new ImageDTO(
                1L,
                "https://example.com/image.jpg",
                null,
                true, // isPrimary = true en DTO
                LocalDateTime.now()
        );

        // When
        ImageEntity entity = imageMapper.toEntity(dtoWithPrimaryTrue);

        // Then
        assertNotNull(entity);
        // A pesar de que el DTO tiene isPrimary=true, la entidad debe tener false (constant)
        assertFalse(entity.getIsPrimary());
    }

    @Test
    @DisplayName("Debe ignorar el campo accommodation al mapear de DTO a Entity")
    void toEntity_shouldIgnoreAccommodation() {
        // When
        ImageEntity entity = imageMapper.toEntity(imageDTO);

        // Then
        assertNotNull(entity);
        assertNull(entity.getAccommodation());
    }

    @Test
    @DisplayName("Debe ignorar campos de Cloudinary al mapear de DTO a Entity")
    void toEntity_shouldIgnoreCloudinaryFields() {
        // When
        ImageEntity entity = imageMapper.toEntity(imageDTO);

        // Then
        assertNotNull(entity);
        assertNull(entity.getCloudinaryPublicId());
        assertNull(entity.getCloudinaryThumbnailUrl());
    }

    @Test
    @DisplayName("Debe manejar URLs largas correctamente")
    void toModel_withLongUrls_shouldMapCorrectly() {
        // Given
        String longUrl = "https://example.com/very/long/path/" + "segment/".repeat(50) + "image.jpg";
        imageEntity.setUrl(longUrl);

        // When
        ImageDTO dto = imageMapper.toModel(imageEntity);

        // Then
        assertNotNull(dto);
        assertEquals(longUrl, dto.url());
    }

    @Test
    @DisplayName("Debe manejar thumbnailUrl null correctamente")
    void toModel_withNullThumbnailUrl_shouldMapCorrectly() {
        // Given
        imageEntity.setThumbnailUrl(null);

        // When
        ImageDTO dto = imageMapper.toModel(imageEntity);

        // Then
        assertNotNull(dto);
        assertNull(dto.thumbnailUrl());
    }

    @Test
    @DisplayName("Debe manejar isPrimary con diferentes valores")
    void toModel_withDifferentIsPrimaryValues_shouldMapCorrectly() {
        // Given - isPrimary = true
        imageEntity.setIsPrimary(true);
        ImageDTO dto1 = imageMapper.toModel(imageEntity);
        assertTrue(dto1.isPrimary());

        // Given - isPrimary = false
        imageEntity.setIsPrimary(false);
        ImageDTO dto2 = imageMapper.toModel(imageEntity);
        assertFalse(dto2.isPrimary());
    }

    @Test
    @DisplayName("Debe aplicar numberFormat correctamente en el ID")
    void toModel_shouldApplyNumberFormat() {
        // Given - Diferentes IDs
        imageEntity.setId(1L);
        ImageDTO dto1 = imageMapper.toModel(imageEntity);
        assertEquals(1L, dto1.id());

        imageEntity.setId(999999L);
        ImageDTO dto2 = imageMapper.toModel(imageEntity);
        assertEquals(999999L, dto2.id());
    }

    @Test
    @DisplayName("Debe mantener consistencia en múltiples llamadas con los mismos datos")
    void toModel_shouldBeConsistentAcrossMultipleCalls() {
        // When - Llamar múltiples veces
        ImageDTO dto1 = imageMapper.toModel(imageEntity);
        ImageDTO dto2 = imageMapper.toModel(imageEntity);

        // Then - Los resultados deben ser iguales
        assertNotNull(dto1);
        assertNotNull(dto2);
        assertEquals(dto1.id(), dto2.id());
        assertEquals(dto1.url(), dto2.url());
        assertEquals(dto1.thumbnailUrl(), dto2.thumbnailUrl());
        assertEquals(dto1.isPrimary(), dto2.isPrimary());
        assertEquals(dto1.createdAt(), dto2.createdAt());
    }

    @Test
    @DisplayName("Debe manejar todas las combinaciones de campos opcionales")
    void toModel_withOptionalFields_shouldMapCorrectly() {
        // Given - Solo campos obligatorios
        ImageEntity minimalEntity = new ImageEntity();
        minimalEntity.setId(1L);
        minimalEntity.setUrl("https://example.com/image.jpg");
        minimalEntity.setIsPrimary(false);
        minimalEntity.setCreatedAt(LocalDateTime.now());
        // thumbnailUrl es null

        // When
        ImageDTO dto = imageMapper.toModel(minimalEntity);

        // Then
        assertNotNull(dto);
        assertEquals(1L, dto.id());
        assertEquals("https://example.com/image.jpg", dto.url());
        assertNull(dto.thumbnailUrl());
        assertFalse(dto.isPrimary());
        assertNotNull(dto.createdAt());
    }

    @Test
    @DisplayName("Debe ignorar correctamente el ID al crear entidad desde DTO")
    void toEntity_shouldIgnoreId() {
        // Given - DTO con ID
        ImageDTO dtoWithId = new ImageDTO(
                999L,
                "https://example.com/image.jpg",
                "https://example.com/thumb.jpg",
                true,
                LocalDateTime.now()
        );

        // When
        ImageEntity entity = imageMapper.toEntity(dtoWithId);

        // Then
        assertNotNull(entity);
        assertNull(entity.getId()); // El ID debe ser null
    }

    @Test
    @DisplayName("Debe ignorar correctamente createdAt al crear entidad desde DTO")
    void toEntity_shouldIgnoreCreatedAt() {
        // Given - DTO con createdAt
        LocalDateTime specificDate = LocalDateTime.of(2020, 1, 1, 0, 0);
        ImageDTO dtoWithDate = new ImageDTO(
                1L,
                "https://example.com/image.jpg",
                null,
                false,
                specificDate
        );

        // When
        ImageEntity entity = imageMapper.toEntity(dtoWithDate);

        // Then
        assertNotNull(entity);
        assertNull(entity.getCreatedAt()); // createdAt debe ser null (se asigna con @PrePersist)
    }

    @Test
    @DisplayName("Debe mapear solo URL y thumbnailURL de DTO a Entity")
    void toEntity_shouldMapOnlyUrlFields() {
        // Given
        String url = "https://example.com/test-image.jpg";
        String thumbnailUrl = "https://example.com/test-thumb.jpg";
        ImageDTO dto = new ImageDTO(100L, url, thumbnailUrl, true, LocalDateTime.now());

        // When
        ImageEntity entity = imageMapper.toEntity(dto);

        // Then
        assertNotNull(entity);
        assertEquals(url, entity.getUrl());
        assertEquals(thumbnailUrl, entity.getThumbnailUrl());

        // Verificar que los demás campos están ignorados o tienen valores por defecto
        assertNull(entity.getId());
        assertNull(entity.getCreatedAt());
        assertNull(entity.getAccommodation());
        assertNull(entity.getCloudinaryPublicId());
        assertNull(entity.getCloudinaryThumbnailUrl());
        assertFalse(entity.getIsPrimary());
    }

    @Test
    @DisplayName("No debe incluir campos de Cloudinary en el DTO")
    void toModel_shouldNotIncludeCloudinaryFieldsInDTO() {
        // Given - Entity con campos de Cloudinary
        imageEntity.setCloudinaryPublicId("cloudinary_id_123");
        imageEntity.setCloudinaryThumbnailUrl("https://cloudinary.com/thumb.jpg");

        // When
        ImageDTO dto = imageMapper.toModel(imageEntity);

        // Then
        assertNotNull(dto);
        // ImageDTO no tiene campos cloudinaryPublicId ni cloudinaryThumbnailUrl
        // Solo debe tener: id, url, thumbnailUrl, isPrimary, createdAt
        assertEquals(imageEntity.getId(), dto.id());
        assertEquals(imageEntity.getUrl(), dto.url());
        assertEquals(imageEntity.getThumbnailUrl(), dto.thumbnailUrl());
        assertEquals(imageEntity.getIsPrimary(), dto.isPrimary());
        assertNotNull(dto.createdAt());
    }
}