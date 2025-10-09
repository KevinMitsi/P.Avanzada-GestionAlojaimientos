package com.avanzada.alojamientos.mappers;

import com.avanzada.alojamientos.DTO.accommodation.AccommodationDTO;
import com.avanzada.alojamientos.DTO.accommodation.CreateAccommodationDTO;
import com.avanzada.alojamientos.DTO.accommodation.CreateAccommodationResponseDTO;
import com.avanzada.alojamientos.DTO.accommodation.UpdateAccommodationDTO;
import com.avanzada.alojamientos.DTO.model.Coordinates;
import com.avanzada.alojamientos.DTO.other.CoordinatesDTO;
import com.avanzada.alojamientos.entities.AccommodationEntity;
import com.avanzada.alojamientos.entities.CityEntity;
import com.avanzada.alojamientos.entities.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AccommodationMapperTest {

    @Autowired
    private AccommodationMapper accommodationMapper;

    private AccommodationEntity accommodationEntity;
    private CreateAccommodationDTO createAccommodationDTO;
    private UpdateAccommodationDTO updateAccommodationDTO;
    private CityEntity cityEntity;
    private UserEntity userEntity;

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
        userEntity.setName("Juan Pérez");
        userEntity.setEmail("juan@example.com");

        // Setup Accommodation Entity
        accommodationEntity = new AccommodationEntity();
        accommodationEntity.setId(1L);
        accommodationEntity.setTitle("Apartamento en el centro");
        accommodationEntity.setDescription("Hermoso apartamento con vista panorámica");
        accommodationEntity.setAddress("Calle 123 #45-67");
        accommodationEntity.setCoordinates(new Coordinates(4.6097, -74.0817));
        accommodationEntity.setPricePerNight(new BigDecimal("150000.00"));
        accommodationEntity.setServices(new HashSet<>(Arrays.asList("WiFi", "Parking", "Pool")));
        accommodationEntity.setMaxGuests(4);
        accommodationEntity.setActive(true);
        accommodationEntity.setSoftDeleted(false);
        accommodationEntity.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
        accommodationEntity.setUpdatedAt(LocalDateTime.of(2025, 1, 15, 14, 30));
        accommodationEntity.setHost(userEntity);
        accommodationEntity.setCity(cityEntity);

        // Setup CreateAccommodationDTO
        createAccommodationDTO = new CreateAccommodationDTO(
                "Nuevo Apartamento",
                "Descripción del nuevo apartamento",
                1L,
                new CoordinatesDTO(4.6097, -74.0817),
                "Calle Nueva #10-20",
                new BigDecimal("200000.00"),
                Arrays.asList("WiFi", "Kitchen"),
                2
        );

        // Setup UpdateAccommodationDTO
        updateAccommodationDTO = new UpdateAccommodationDTO(
                "Apartamento Actualizado",
                "Descripción actualizada",
                "Calle Actualizada #30-40",
                new CoordinatesDTO(4.7110, -74.0721),
                new BigDecimal("180000.00"),
                Arrays.asList("WiFi", "AC", "Heating"),
                3,
                false
        );
    }

    @Test
    @DisplayName("Debe mapear AccommodationEntity a AccommodationDTO correctamente")
    void toAccommodationDTO_shouldMapCorrectly() {
        // When
        AccommodationDTO dto = accommodationMapper.toAccommodationDTO(accommodationEntity);

        // Then
        assertNotNull(dto);
        assertEquals(accommodationEntity.getId(), dto.id());
        assertEquals(accommodationEntity.getHost().getId(), dto.hostId());
        assertEquals(accommodationEntity.getTitle(), dto.title());
        assertEquals(accommodationEntity.getDescription(), dto.description());
        assertEquals(accommodationEntity.getAddress(), dto.address());
        assertEquals(accommodationEntity.getPricePerNight(), dto.pricePerNight());
        assertEquals(accommodationEntity.getMaxGuests(), dto.maxGuests());
        assertEquals(accommodationEntity.getActive(), dto.active());
        assertEquals(accommodationEntity.getSoftDeleted(), dto.softDeleted());

        // Verificar city
        assertNotNull(dto.city());
        assertEquals(cityEntity.getId(), dto.city().id());
        assertEquals(cityEntity.getName(), dto.city().name());
        assertEquals(cityEntity.getCountry(), dto.city().country());

        // Verificar coordinates
        assertNotNull(dto.coordinates());
        assertEquals(accommodationEntity.getCoordinates().getLat(), dto.coordinates().lat());
        assertEquals(accommodationEntity.getCoordinates().getLng(), dto.coordinates().lng());
    }

    @Test
    @DisplayName("Debe retornar null cuando AccommodationEntity es null")
    void toAccommodationDTO_withNullEntity_shouldReturnNull() {
        // When
        AccommodationDTO dto = accommodationMapper.toAccommodationDTO(null);

        // Then
        assertNull(dto);
    }

    @Test
    @DisplayName("Debe mapear AccommodationEntity a CreateAccommodationResponseDTO correctamente")
    void toCreateAccommodationResponseDTO_shouldMapCorrectly() {
        // When
        CreateAccommodationResponseDTO dto = accommodationMapper.toCreateAccommodationResponseDTO(accommodationEntity);

        // Then
        assertNotNull(dto);
        assertEquals(accommodationEntity.getId(), dto.id());
        assertEquals(accommodationEntity.getHost().getId(), dto.hostId());
        assertEquals(accommodationEntity.getTitle(), dto.title());
        assertEquals(accommodationEntity.getDescription(), dto.description());
        assertEquals(accommodationEntity.getAddress(), dto.address());
        assertEquals(accommodationEntity.getPricePerNight(), dto.pricePerNight());
        assertEquals(accommodationEntity.getMaxGuests(), dto.maxGuests());

        // Verificar city
        assertNotNull(dto.city());
        assertEquals(cityEntity.getId(), dto.city().id());

        // Verificar coordinates
        assertNotNull(dto.coordinates());
        assertEquals(accommodationEntity.getCoordinates().getLat(), dto.coordinates().lat());
        assertEquals(accommodationEntity.getCoordinates().getLng(), dto.coordinates().lng());

        // Verificar services
        assertNotNull(dto.services());
        assertEquals(3, dto.services().size());
    }

    @Test
    @DisplayName("Debe mapear CreateAccommodationDTO a AccommodationEntity correctamente")
    void toEntity_shouldMapCorrectly() {
        // When
        AccommodationEntity entity = accommodationMapper.toEntity(createAccommodationDTO);

        // Then
        assertNotNull(entity);
        assertNull(entity.getId()); // ID debe ser null (se asigna en la BD)
        assertEquals(createAccommodationDTO.title(), entity.getTitle());
        assertEquals(createAccommodationDTO.description(), entity.getDescription());
        assertEquals(createAccommodationDTO.address(), entity.getAddress());
        assertEquals(createAccommodationDTO.pricePerNight(), entity.getPricePerNight());
        assertEquals(createAccommodationDTO.maxGuests(), entity.getMaxGuests());

        // Verificar valores por defecto
        assertTrue(entity.getActive());
        assertFalse(entity.getSoftDeleted());
        assertNotNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());
        assertNull(entity.getDeletedAt());
        assertNull(entity.getHost()); // Se ignora en el mapeo

        // Verificar city
        assertNotNull(entity.getCity());
        assertEquals(createAccommodationDTO.city(), entity.getCity().getId());

        // Verificar coordinates
        assertNotNull(entity.getCoordinates());
        assertEquals(createAccommodationDTO.coordinates().lat(), entity.getCoordinates().getLat());
        assertEquals(createAccommodationDTO.coordinates().lng(), entity.getCoordinates().getLng());
    }

    @Test
    @DisplayName("Debe retornar null cuando CreateAccommodationDTO es null")
    void toEntity_withNullDTO_shouldReturnNull() {
        // When
        AccommodationEntity entity = accommodationMapper.toEntity(null);

        // Then
        assertNull(entity);
    }

    @Test
    @DisplayName("Debe actualizar entidad desde UpdateAccommodationDTO correctamente")
    void updateEntityFromDTO_shouldUpdateOnlyProvidedFields() {
        // Given - Crear una copia de la entidad original para comparar
        String originalAddress = accommodationEntity.getAddress();
        BigDecimal originalPrice = accommodationEntity.getPricePerNight();

        // When
        accommodationMapper.updateEntityFromDTO(updateAccommodationDTO, accommodationEntity);

        // Then
        // Campos que deben actualizarse
        assertEquals(updateAccommodationDTO.title(), accommodationEntity.getTitle());
        assertEquals(updateAccommodationDTO.description(), accommodationEntity.getDescription());
        assertEquals(updateAccommodationDTO.address(), accommodationEntity.getAddress());
        assertEquals(updateAccommodationDTO.pricePerNight(), accommodationEntity.getPricePerNight());
        assertEquals(updateAccommodationDTO.maxGuests(), accommodationEntity.getMaxGuests());
        assertEquals(updateAccommodationDTO.active(), accommodationEntity.getActive());

        // Verificar que address y price cambiaron
        assertNotEquals(originalAddress, accommodationEntity.getAddress());
        assertNotEquals(originalPrice, accommodationEntity.getPricePerNight());

        // Campos que NO deben cambiar (ignorados)
        assertEquals(1L, accommodationEntity.getId());
        assertEquals(userEntity, accommodationEntity.getHost());
        assertEquals(cityEntity, accommodationEntity.getCity());
        assertFalse(accommodationEntity.getSoftDeleted());
        assertNotNull(accommodationEntity.getCreatedAt());
    }

    @Test
    @DisplayName("Debe actualizar solo campos no nulos (IGNORE null values)")
    void updateEntityFromDTO_withNullValues_shouldNotUpdateNullFields() {
        // Given - DTO con solo algunos campos (el resto serán null en un record)
        UpdateAccommodationDTO partialDTO = new UpdateAccommodationDTO(
                "Solo título actualizado",
                "Solo descripción actualizada",
                null, // address null
                null, // coordinates null
                new BigDecimal("250000.00"),
                null, // services null
                5,
                null // active null
        );

        String originalAddress = accommodationEntity.getAddress();
        Boolean originalActive = accommodationEntity.getActive();

        // When
        accommodationMapper.updateEntityFromDTO(partialDTO, accommodationEntity);

        // Then
        assertEquals("Solo título actualizado", accommodationEntity.getTitle());
        assertEquals("Solo descripción actualizada", accommodationEntity.getDescription());
        assertEquals(new BigDecimal("250000.00"), accommodationEntity.getPricePerNight());
        assertEquals(5, accommodationEntity.getMaxGuests());

        // Estos NO deben cambiar porque son null en el DTO
        assertEquals(originalAddress, accommodationEntity.getAddress());
        assertEquals(originalActive, accommodationEntity.getActive());
    }

    @Test
    @DisplayName("Debe mapear cityId a CityEntity correctamente usando el método map personalizado")
    void map_shouldCreateCityEntityWithId() {
        // When
        CityEntity city = accommodationMapper.map(5L);

        // Then
        assertNotNull(city);
        assertEquals(5L, city.getId());
    }

    @Test
    @DisplayName("Debe retornar null cuando cityId es null")
    void map_withNullId_shouldReturnNull() {
        // When
        CityEntity city = accommodationMapper.map(null);

        // Then
        assertNull(city);
    }

    @Test
    @DisplayName("Debe manejar servicios vacíos correctamente")
    void toEntity_withEmptyServices_shouldHandleCorrectly() {
        // Given
        CreateAccommodationDTO dtoWithoutServices = new CreateAccommodationDTO(
                "Apartamento sin servicios",
                "Descripción básica",
                1L,
                new CoordinatesDTO(4.6, -74.0),
                "Calle Sin Servicios #1-1",
                new BigDecimal("100000.00"),
                List.of(), // Lista vacía
                1
        );

        // When
        AccommodationEntity entity = accommodationMapper.toEntity(dtoWithoutServices);

        // Then
        assertNotNull(entity);
        assertNotNull(entity.getServices());
        assertTrue(entity.getServices().isEmpty());
    }

    @Test
    @DisplayName("Debe manejar coordinates nulos en la entidad")
    void toAccommodationDTO_withNullCoordinates_shouldHandleCorrectly() {
        // Given
        accommodationEntity.setCoordinates(null);

        // When
        AccommodationDTO dto = accommodationMapper.toAccommodationDTO(accommodationEntity);

        // Then
        assertNotNull(dto);
        assertNull(dto.coordinates());
    }
}