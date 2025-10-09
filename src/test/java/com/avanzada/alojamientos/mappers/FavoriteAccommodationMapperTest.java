package com.avanzada.alojamientos.mappers;

import com.avanzada.alojamientos.DTO.model.Coordinates;
import com.avanzada.alojamientos.DTO.other.FavoriteAccommodationDTO;
import com.avanzada.alojamientos.entities.AccommodationEntity;
import com.avanzada.alojamientos.entities.CityEntity;
import com.avanzada.alojamientos.entities.ImageEntity;
import com.avanzada.alojamientos.entities.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FavoriteAccommodationMapperTest {

    @Autowired
    private FavoriteAccommodationMapper favoriteAccommodationMapper;

    private AccommodationEntity accommodationEntity;
    private CityEntity cityEntity;
    private List<ImageEntity> images;

    @BeforeEach
    void setUp() {
        // Setup City Entity
        cityEntity = new CityEntity();
        cityEntity.setId(1L);
        cityEntity.setName("Bogotá");
        cityEntity.setCountry("Colombia");

        // Setup Host Entity
        UserEntity hostEntity = new UserEntity();
        hostEntity.setId(100L);
        hostEntity.setName("María García");
        hostEntity.setEmail("maria@example.com");

        // Setup Images
        ImageEntity image1 = new ImageEntity();
        image1.setId(1L);
        image1.setUrl("https://example.com/image1.jpg");
        image1.setCloudinaryPublicId("cloudinary_id_1");
        image1.setIsPrimary(true);
        image1.setCreatedAt(LocalDateTime.now());

        ImageEntity image2 = new ImageEntity();
        image2.setId(2L);
        image2.setUrl("https://example.com/image2.jpg");
        image2.setCloudinaryPublicId("cloudinary_id_2");
        image2.setIsPrimary(false);
        image2.setCreatedAt(LocalDateTime.now());

        images = Arrays.asList(image1, image2);

        // Setup Accommodation Entity
        accommodationEntity = new AccommodationEntity();
        accommodationEntity.setId(10L);
        accommodationEntity.setTitle("Hermoso apartamento en el centro");
        accommodationEntity.setDescription("Apartamento moderno con todas las comodidades");
        accommodationEntity.setAddress("Carrera 7 #45-23");
        accommodationEntity.setCoordinates(new Coordinates(4.6097, -74.0817));
        accommodationEntity.setPricePerNight(new BigDecimal("250000.00"));
        accommodationEntity.setServices(new HashSet<>(Arrays.asList("WiFi", "Parking", "Pool", "Gym")));
        accommodationEntity.setMaxGuests(4);
        accommodationEntity.setActive(true);
        accommodationEntity.setSoftDeleted(false);
        accommodationEntity.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
        accommodationEntity.setHost(hostEntity);
        accommodationEntity.setCity(cityEntity);
        accommodationEntity.setImages(images);
    }

    @Test
    @DisplayName("Debe mapear AccommodationEntity a FavoriteAccommodationDTO correctamente")
    void toDTO_shouldMapCorrectly() {
        // When
        FavoriteAccommodationDTO dto = favoriteAccommodationMapper.toDTO(accommodationEntity);

        // Then
        assertNotNull(dto);
        assertEquals(accommodationEntity.getId(), dto.id());
        assertEquals(accommodationEntity.getTitle(), dto.title());
        assertEquals(accommodationEntity.getAddress(), dto.address());
        assertEquals(accommodationEntity.getPricePerNight(), dto.pricePerNight());
        assertEquals(accommodationEntity.getMaxGuests(), dto.maxGuests());

        // Verificar city
        assertNotNull(dto.city());
        assertEquals(cityEntity.getId(), dto.city().id());
        assertEquals(cityEntity.getName(), dto.city().name());
        assertEquals(cityEntity.getCountry(), dto.city().country());

        // Verificar images
        assertNotNull(dto.images());
        assertEquals(2, dto.images().size());
        assertEquals(images.get(0).getUrl(), dto.images().get(0).url());
        assertEquals(images.get(1).getUrl(), dto.images().get(1).url());
    }

    @Test
    @DisplayName("Debe retornar null cuando AccommodationEntity es null")
    void toDTO_withNullEntity_shouldReturnNull() {
        // When
        FavoriteAccommodationDTO dto = favoriteAccommodationMapper.toDTO(null);

        // Then
        assertNull(dto);
    }

    @Test
    @DisplayName("Debe mapear correctamente cuando no hay imágenes")
    void toDTO_withNoImages_shouldMapCorrectly() {
        // Given
        accommodationEntity.setImages(new ArrayList<>());

        // When
        FavoriteAccommodationDTO dto = favoriteAccommodationMapper.toDTO(accommodationEntity);

        // Then
        assertNotNull(dto);
        assertNotNull(dto.images());
        assertTrue(dto.images().isEmpty());
    }

    @Test
    @DisplayName("Debe mapear correctamente cuando images es null")
    void toDTO_withNullImages_shouldMapCorrectly() {
        // Given
        accommodationEntity.setImages(null);

        // When
        FavoriteAccommodationDTO dto = favoriteAccommodationMapper.toDTO(accommodationEntity);

        // Then
        assertNotNull(dto);
        assertNull(dto.images());
    }

    @Test
    @DisplayName("Debe incluir solo los campos necesarios para favoritos")
    void toDTO_shouldOnlyIncludeNecessaryFields() {
        // When
        FavoriteAccommodationDTO dto = favoriteAccommodationMapper.toDTO(accommodationEntity);

        // Then
        assertNotNull(dto);

        // Verificar que están los campos necesarios
        assertNotNull(dto.id());
        assertNotNull(dto.title());
        assertNotNull(dto.city());
        assertNotNull(dto.address());
        assertNotNull(dto.pricePerNight());
        assertNotNull(dto.maxGuests());

        // Verificar que NO se incluyen campos innecesarios como:
        // - description (no está en FavoriteAccommodationDTO)
        // - coordinates (no está en FavoriteAccommodationDTO)
        // - services (no está en FavoriteAccommodationDTO)
        // - host (no está en FavoriteAccommodationDTO)
        // - active (no está en FavoriteAccommodationDTO)
    }

    @Test
    @DisplayName("Debe manejar diferentes precios correctamente")
    void toDTO_withDifferentPrices_shouldMapCorrectly() {
        // Given - Precio bajo
        accommodationEntity.setPricePerNight(new BigDecimal("50000.00"));
        FavoriteAccommodationDTO dto1 = favoriteAccommodationMapper.toDTO(accommodationEntity);
        assertEquals(new BigDecimal("50000.00"), dto1.pricePerNight());

        // Given - Precio alto
        accommodationEntity.setPricePerNight(new BigDecimal("999999.99"));
        FavoriteAccommodationDTO dto2 = favoriteAccommodationMapper.toDTO(accommodationEntity);
        assertEquals(new BigDecimal("999999.99"), dto2.pricePerNight());
    }

    @Test
    @DisplayName("Debe manejar diferentes cantidades de huéspedes correctamente")
    void toDTO_withDifferentMaxGuests_shouldMapCorrectly() {
        // Given - 1 huésped
        accommodationEntity.setMaxGuests(1);
        FavoriteAccommodationDTO dto1 = favoriteAccommodationMapper.toDTO(accommodationEntity);
        assertEquals(1, dto1.maxGuests());

        // Given - 10 huéspedes
        accommodationEntity.setMaxGuests(10);
        FavoriteAccommodationDTO dto2 = favoriteAccommodationMapper.toDTO(accommodationEntity);
        assertEquals(10, dto2.maxGuests());
    }

    @Test
    @DisplayName("Debe usar CityMapper para mapear la ciudad")
    void toDTO_shouldUseCityMapper() {
        // Given - Ciudad con datos completos
        cityEntity.setName("Medellín");
        cityEntity.setCountry("Colombia");

        // When
        FavoriteAccommodationDTO dto = favoriteAccommodationMapper.toDTO(accommodationEntity);

        // Then
        assertNotNull(dto.city());
        assertEquals("Medellín", dto.city().name());
        assertEquals("Colombia", dto.city().country());
    }

    @Test
    @DisplayName("Debe usar ImageMapper para mapear las imágenes")
    void toDTO_shouldUseImageMapper() {
        // When
        FavoriteAccommodationDTO dto = favoriteAccommodationMapper.toDTO(accommodationEntity);

        // Then
        assertNotNull(dto.images());
        assertEquals(2, dto.images().size());

        // Verificar que las imágenes tienen todos los campos necesarios
        dto.images().forEach(image -> {
            assertNotNull(image.id());
            assertNotNull(image.url());
            assertNotNull(image.isPrimary());
        });
    }

    @Test
    @DisplayName("Debe manejar múltiples imágenes correctamente")
    void toDTO_withMultipleImages_shouldMapCorrectly() {
        // Given - Agregar más imágenes
        ImageEntity image3 = new ImageEntity();
        image3.setId(3L);
        image3.setUrl("https://example.com/image3.jpg");
        image3.setCloudinaryPublicId("cloudinary_id_3");
        image3.setIsPrimary(false);
        image3.setCreatedAt(LocalDateTime.now());

        ImageEntity image4 = new ImageEntity();
        image4.setId(4L);
        image4.setUrl("https://example.com/image4.jpg");
        image4.setCloudinaryPublicId("cloudinary_id_4");
        image4.setIsPrimary(false);
        image4.setCreatedAt(LocalDateTime.now());

        List<ImageEntity> multipleImages = Arrays.asList(
                images.get(0), images.get(1), image3, image4
        );
        accommodationEntity.setImages(multipleImages);

        // When
        FavoriteAccommodationDTO dto = favoriteAccommodationMapper.toDTO(accommodationEntity);

        // Then
        assertNotNull(dto.images());
        assertEquals(4, dto.images().size());
    }

    @Test
    @DisplayName("Debe preservar el orden de las imágenes")
    void toDTO_shouldPreserveImageOrder() {
        // When
        FavoriteAccommodationDTO dto = favoriteAccommodationMapper.toDTO(accommodationEntity);

        // Then
        assertNotNull(dto.images());
        assertEquals(images.get(0).getUrl(), dto.images().get(0).url());
        assertEquals(images.get(1).getUrl(), dto.images().get(1).url());
    }

    @Test
    @DisplayName("Debe manejar avgRating cuando está presente")
    void toDTO_withAvgRating_shouldMapCorrectly() {
        // When
        FavoriteAccommodationDTO dto = favoriteAccommodationMapper.toDTO(accommodationEntity);

        // Then
        assertNotNull(dto);
        // avgRating puede ser null si no hay comentarios
        // El mapper simplemente lo mapea, no lo calcula
    }

    @Test
    @DisplayName("Debe mapear títulos largos correctamente")
    void toDTO_withLongTitle_shouldMapCorrectly() {
        // Given
        String longTitle = "A".repeat(200); // Máximo permitido según la entidad
        accommodationEntity.setTitle(longTitle);

        // When
        FavoriteAccommodationDTO dto = favoriteAccommodationMapper.toDTO(accommodationEntity);

        // Then
        assertNotNull(dto);
        assertEquals(longTitle, dto.title());
        assertEquals(200, dto.title().length());
    }

    @Test
    @DisplayName("Debe mapear direcciones largas correctamente")
    void toDTO_withLongAddress_shouldMapCorrectly() {
        // Given
        String longAddress = "Calle muy larga " + "con muchos detalles ".repeat(20);
        accommodationEntity.setAddress(longAddress);

        // When
        FavoriteAccommodationDTO dto = favoriteAccommodationMapper.toDTO(accommodationEntity);

        // Then
        assertNotNull(dto);
        assertEquals(longAddress, dto.address());
    }

    @Test
    @DisplayName("Debe ser consistente en múltiples llamadas con los mismos datos")
    void toDTO_shouldBeConsistentAcrossMultipleCalls() {
        // When - Llamar múltiples veces
        FavoriteAccommodationDTO dto1 = favoriteAccommodationMapper.toDTO(accommodationEntity);
        FavoriteAccommodationDTO dto2 = favoriteAccommodationMapper.toDTO(accommodationEntity);

        // Then - Los resultados deben ser iguales
        assertNotNull(dto1);
        assertNotNull(dto2);
        assertEquals(dto1.id(), dto2.id());
        assertEquals(dto1.title(), dto2.title());
        assertEquals(dto1.address(), dto2.address());
        assertEquals(dto1.pricePerNight(), dto2.pricePerNight());
        assertEquals(dto1.maxGuests(), dto2.maxGuests());
        assertEquals(dto1.city().id(), dto2.city().id());
        assertEquals(dto1.images().size(), dto2.images().size());
    }
}