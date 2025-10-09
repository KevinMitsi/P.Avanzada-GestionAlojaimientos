package com.avanzada.alojamientos.mappers;

import com.avanzada.alojamientos.DTO.model.Coordinates;
import com.avanzada.alojamientos.DTO.other.FavoriteDTO;
import com.avanzada.alojamientos.entities.AccommodationEntity;
import com.avanzada.alojamientos.entities.CityEntity;
import com.avanzada.alojamientos.entities.FavoriteEntity;
import com.avanzada.alojamientos.entities.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FavoriteMapperTest {

    @Autowired
    private FavoriteMapper favoriteMapper;

    private FavoriteEntity favoriteEntity;
    private UserEntity userEntity;
    private AccommodationEntity accommodationEntity;
    private CityEntity cityEntity;

    @BeforeEach
    void setUp() {
        // Setup City Entity
        cityEntity = new CityEntity();
        cityEntity.setId(1L);
        cityEntity.setName("Bogotá");
        cityEntity.setCountry("Colombia");

        // Setup User Entity
        userEntity = new UserEntity();
        userEntity.setId(100L);
        userEntity.setName("Carlos Rodríguez");
        userEntity.setEmail("carlos@example.com");

        // Setup Accommodation Entity
        accommodationEntity = new AccommodationEntity();
        accommodationEntity.setId(50L);
        accommodationEntity.setTitle("Apartamento moderno");
        accommodationEntity.setDescription("Hermoso apartamento en el centro");
        accommodationEntity.setAddress("Calle 50 #20-30");
        accommodationEntity.setCoordinates(new Coordinates(4.6097, -74.0817));
        accommodationEntity.setPricePerNight(new BigDecimal("200000.00"));
        accommodationEntity.setServices(new HashSet<>());
        accommodationEntity.setMaxGuests(3);
        accommodationEntity.setActive(true);
        accommodationEntity.setSoftDeleted(false);
        accommodationEntity.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
        accommodationEntity.setCity(cityEntity);
        accommodationEntity.setImages(new ArrayList<>());

        // Setup Favorite Entity
        favoriteEntity = new FavoriteEntity();
        favoriteEntity.setId(10L);
        favoriteEntity.setUser(userEntity);
        favoriteEntity.setAccommodation(accommodationEntity);
        favoriteEntity.setCreatedAt(LocalDateTime.of(2025, 6, 15, 14, 30, 45));
    }

    @Test
    @DisplayName("Debe mapear FavoriteEntity a FavoriteDTO correctamente")
    void toDTO_shouldMapCorrectly() {
        // When
        FavoriteDTO dto = favoriteMapper.toDTO(favoriteEntity);

        // Then
        assertNotNull(dto);
        assertEquals(favoriteEntity.getId(), dto.id());
        assertEquals(favoriteEntity.getUser().getId(), dto.userId());

        // Verificar accommodation (debe usar FavoriteAccommodationMapper)
        assertNotNull(dto.accommodation());
        assertEquals(accommodationEntity.getId(), dto.accommodation().id());
        assertEquals(accommodationEntity.getTitle(), dto.accommodation().title());
        assertEquals(accommodationEntity.getAddress(), dto.accommodation().address());
        assertEquals(accommodationEntity.getPricePerNight(), dto.accommodation().pricePerNight());

        // Verificar formato de fecha: "yyyy-MM-dd'T'HH:mm:ss"
        assertNotNull(dto.createdA());
        assertEquals("2025-06-15T14:30:45", dto.createdA());
    }

    @Test
    @DisplayName("Debe retornar null cuando FavoriteEntity es null")
    void toDTO_withNullEntity_shouldReturnNull() {
        // When
        FavoriteDTO dto = favoriteMapper.toDTO(null);

        // Then
        assertNull(dto);
    }

    @Test
    @DisplayName("Debe formatear correctamente diferentes fechas")
    void toDTO_shouldFormatDifferentDatesCorrectly() {
        // Given - Fecha específica: 1 de enero de 2025 a las 00:00:00
        favoriteEntity.setCreatedAt(LocalDateTime.of(2025, 1, 1, 0, 0, 0));
        FavoriteDTO dto1 = favoriteMapper.toDTO(favoriteEntity);
        assertEquals("2025-01-01T00:00:00", dto1.createdA());

        // Given - Fecha con hora completa: 31 de diciembre de 2025 a las 23:59:59
        favoriteEntity.setCreatedAt(LocalDateTime.of(2025, 12, 31, 23, 59, 59));
        FavoriteDTO dto2 = favoriteMapper.toDTO(favoriteEntity);
        assertEquals("2025-12-31T23:59:59", dto2.createdA());

        // Given - Fecha con hora del mediodía
        favoriteEntity.setCreatedAt(LocalDateTime.of(2025, 7, 20, 12, 0, 0));
        FavoriteDTO dto3 = favoriteMapper.toDTO(favoriteEntity);
        assertEquals("2025-07-20T12:00:00", dto3.createdA());
    }

    @Test
    @DisplayName("Debe usar FavoriteAccommodationMapper para mapear el alojamiento")
    void toDTO_shouldUseFavoriteAccommodationMapper() {
        // When
        FavoriteDTO dto = favoriteMapper.toDTO(favoriteEntity);

        // Then
        assertNotNull(dto.accommodation());

        // Verificar que se incluyen solo los campos de FavoriteAccommodationDTO
        assertNotNull(dto.accommodation().id());
        assertNotNull(dto.accommodation().title());
        assertNotNull(dto.accommodation().city());
        assertNotNull(dto.accommodation().address());
        assertNotNull(dto.accommodation().pricePerNight());
        assertNotNull(dto.accommodation().maxGuests());
    }

    @Test
    @DisplayName("Debe mapear correctamente el userId desde user.id")
    void toDTO_shouldMapUserIdCorrectly() {
        // Given - Diferentes IDs de usuario
        userEntity.setId(999L);
        FavoriteDTO dto1 = favoriteMapper.toDTO(favoriteEntity);
        assertEquals(999L, dto1.userId());

        userEntity.setId(1L);
        FavoriteDTO dto2 = favoriteMapper.toDTO(favoriteEntity);
        assertEquals(1L, dto2.userId());
    }

    @Test
    @DisplayName("Debe mapear toEntity ignorando campos específicos")
    void toEntity_shouldIgnoreSpecificFields() {
        // Given - Crear un DTO (aunque en la práctica no se usa este método)
        FavoriteDTO dto = favoriteMapper.toDTO(favoriteEntity);

        // When
        FavoriteEntity entity = favoriteMapper.toEntity(dto);

        // Then
        assertNotNull(entity);

        // Verificar que los campos ignorados son null (se asignan en el servicio)
        assertNull(entity.getId());
        assertNull(entity.getCreatedAt());
        assertNull(entity.getUser());
        assertNull(entity.getAccommodation());
    }

    @Test
    @DisplayName("Debe retornar null cuando FavoriteDTO es null en toEntity")
    void toEntity_withNullDTO_shouldReturnNull() {
        // When
        FavoriteEntity entity = favoriteMapper.toEntity(null);

        // Then
        assertNull(entity);
    }

    @Test
    @DisplayName("Debe mantener consistencia en múltiples llamadas con los mismos datos")
    void toDTO_shouldBeConsistentAcrossMultipleCalls() {
        // When - Llamar múltiples veces
        FavoriteDTO dto1 = favoriteMapper.toDTO(favoriteEntity);
        FavoriteDTO dto2 = favoriteMapper.toDTO(favoriteEntity);

        // Then - Los resultados deben ser iguales
        assertNotNull(dto1);
        assertNotNull(dto2);
        assertEquals(dto1.id(), dto2.id());
        assertEquals(dto1.userId(), dto2.userId());
        assertEquals(dto1.createdA(), dto2.createdA());
        assertEquals(dto1.accommodation().id(), dto2.accommodation().id());
    }

    @Test
    @DisplayName("Debe manejar accommodation con imágenes correctamente")
    void toDTO_withAccommodationImages_shouldMapCorrectly() {
        // Given - Accommodation ya tiene images configuradas como lista vacía
        assertEquals(0, accommodationEntity.getImages().size());

        // When
        FavoriteDTO dto = favoriteMapper.toDTO(favoriteEntity);

        // Then
        assertNotNull(dto.accommodation());
        assertNotNull(dto.accommodation().images());
        assertTrue(dto.accommodation().images().isEmpty());
    }

    @Test
    @DisplayName("Debe mapear favoritos de diferentes usuarios correctamente")
    void toDTO_withDifferentUsers_shouldMapCorrectly() {
        // Given - Usuario 1
        UserEntity user1 = new UserEntity();
        user1.setId(1L);
        user1.setName("Usuario 1");
        favoriteEntity.setUser(user1);

        FavoriteDTO dto1 = favoriteMapper.toDTO(favoriteEntity);
        assertEquals(1L, dto1.userId());

        // Given - Usuario 2
        UserEntity user2 = new UserEntity();
        user2.setId(2L);
        user2.setName("Usuario 2");
        favoriteEntity.setUser(user2);

        FavoriteDTO dto2 = favoriteMapper.toDTO(favoriteEntity);
        assertEquals(2L, dto2.userId());
    }

    @Test
    @DisplayName("Debe mapear favoritos con diferentes alojamientos correctamente")
    void toDTO_withDifferentAccommodations_shouldMapCorrectly() {
        // Given - Alojamiento 1
        AccommodationEntity accommodation1 = new AccommodationEntity();
        accommodation1.setId(1L);
        accommodation1.setTitle("Alojamiento 1");
        accommodation1.setAddress("Dirección 1");
        accommodation1.setPricePerNight(new BigDecimal("100000.00"));
        accommodation1.setMaxGuests(2);
        accommodation1.setCity(cityEntity);
        accommodation1.setImages(new ArrayList<>());
        favoriteEntity.setAccommodation(accommodation1);

        FavoriteDTO dto1 = favoriteMapper.toDTO(favoriteEntity);
        assertEquals(1L, dto1.accommodation().id());
        assertEquals("Alojamiento 1", dto1.accommodation().title());

        // Given - Alojamiento 2
        AccommodationEntity accommodation2 = new AccommodationEntity();
        accommodation2.setId(2L);
        accommodation2.setTitle("Alojamiento 2");
        accommodation2.setAddress("Dirección 2");
        accommodation2.setPricePerNight(new BigDecimal("200000.00"));
        accommodation2.setMaxGuests(4);
        accommodation2.setCity(cityEntity);
        accommodation2.setImages(new ArrayList<>());
        favoriteEntity.setAccommodation(accommodation2);

        FavoriteDTO dto2 = favoriteMapper.toDTO(favoriteEntity);
        assertEquals(2L, dto2.accommodation().id());
        assertEquals("Alojamiento 2", dto2.accommodation().title());
    }

    @Test
    @DisplayName("Debe mapear correctamente el campo createdA con el formato ISO-8601")
    void toDTO_shouldFormatCreatedAAsISO8601() {
        // Given
        favoriteEntity.setCreatedAt(LocalDateTime.of(2025, 10, 9, 8, 15, 30));

        // When
        FavoriteDTO dto = favoriteMapper.toDTO(favoriteEntity);

        // Then
        assertNotNull(dto.createdA());
        assertEquals("2025-10-09T08:15:30", dto.createdA());

        // Verificar que tiene el formato correcto (año-mes-día T hora:minuto:segundo)
        assertTrue(dto.createdA().matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}"));
    }

    @Test
    @DisplayName("Debe mapear el ID del favorito correctamente")
    void toDTO_shouldMapIdCorrectly() {
        // Given - Diferentes IDs
        favoriteEntity.setId(1L);
        FavoriteDTO dto1 = favoriteMapper.toDTO(favoriteEntity);
        assertEquals(1L, dto1.id());

        favoriteEntity.setId(999999L);
        FavoriteDTO dto2 = favoriteMapper.toDTO(favoriteEntity);
        assertEquals(999999L, dto2.id());
    }

    @Test
    @DisplayName("Debe incluir información completa del alojamiento incluyendo ciudad")
    void toDTO_shouldIncludeCompleteAccommodationInfo() {
        // When
        FavoriteDTO dto = favoriteMapper.toDTO(favoriteEntity);

        // Then
        assertNotNull(dto.accommodation());
        assertNotNull(dto.accommodation().city());
        assertEquals("Bogotá", dto.accommodation().city().name());
        assertEquals("Colombia", dto.accommodation().city().country());
    }
}