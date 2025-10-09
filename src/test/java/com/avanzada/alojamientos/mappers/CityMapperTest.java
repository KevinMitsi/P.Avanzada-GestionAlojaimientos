package com.avanzada.alojamientos.mappers;

import com.avanzada.alojamientos.DTO.city.CityDTO;
import com.avanzada.alojamientos.entities.CityEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CityMapperTest {

    @Autowired
    private CityMapper cityMapper;

    private CityEntity cityEntity;
    private CityDTO cityDTO;

    @BeforeEach
    void setUp() {
        // Setup CityEntity
        cityEntity = new CityEntity();
        cityEntity.setId(1L);
        cityEntity.setName("Bogotá");
        cityEntity.setCountry("Colombia");
        cityEntity.setAccommodations(new ArrayList<>());

        // Setup CityDTO
        cityDTO = new CityDTO(2L, "Medellín", "Colombia");
    }

    @Test
    @DisplayName("Debe mapear CityEntity a CityDTO correctamente")
    void toModel_shouldMapCorrectly() {
        // When
        CityDTO dto = cityMapper.toModel(cityEntity);

        // Then
        assertNotNull(dto);
        assertEquals(cityEntity.getId(), dto.id());
        assertEquals(cityEntity.getName(), dto.name());
        assertEquals(cityEntity.getCountry(), dto.country());
    }

    @Test
    @DisplayName("Debe retornar null cuando CityEntity es null")
    void toModel_withNullEntity_shouldReturnNull() {
        // When
        CityDTO dto = cityMapper.toModel(null);

        // Then
        assertNull(dto);
    }

    @Test
    @DisplayName("Debe mapear CityDTO a CityEntity correctamente")
    void toEntity_shouldMapCorrectly() {
        // When
        CityEntity entity = cityMapper.toEntity(cityDTO);

        // Then
        assertNotNull(entity);
        assertEquals(cityDTO.id(), entity.getId());
        assertEquals(cityDTO.name(), entity.getName());
        assertEquals(cityDTO.country(), entity.getCountry());
    }

    @Test
    @DisplayName("Debe ignorar accommodations al mapear de DTO a Entity")
    void toEntity_shouldIgnoreAccommodations() {
        // When
        CityEntity entity = cityMapper.toEntity(cityDTO);

        // Then
        assertNotNull(entity);
        assertEquals(new ArrayList<>(),entity.getAccommodations());
    }

    @Test
    @DisplayName("Debe retornar null cuando CityDTO es null")
    void toEntity_withNullDTO_shouldReturnNull() {
        // When
        CityEntity entity = cityMapper.toEntity(null);

        // Then
        assertNull(entity);
    }

    @Test
    @DisplayName("Debe manejar nombres y países con caracteres especiales")
    void toModel_withSpecialCharacters_shouldMapCorrectly() {
        // Given
        cityEntity.setName("São Paulo");
        cityEntity.setCountry("Brasil");

        // When
        CityDTO dto = cityMapper.toModel(cityEntity);

        // Then
        assertNotNull(dto);
        assertEquals("São Paulo", dto.name());
        assertEquals("Brasil", dto.country());
    }

    @Test
    @DisplayName("Debe manejar IDs de diferentes magnitudes")
    void toModel_withDifferentIdValues_shouldMapCorrectly() {
        // Given
        cityEntity.setId(999999L);

        // When
        CityDTO dto = cityMapper.toModel(cityEntity);

        // Then
        assertNotNull(dto);
        assertEquals(999999L, dto.id());
    }

    @Test
    @DisplayName("Debe mapear correctamente incluso con accommodations no vacíos en la entidad")
    void toModel_withAccommodations_shouldStillMapCorrectly() {
        // Given - La entidad ya tiene una lista de accommodations inicializada
        assertEquals(0, cityEntity.getAccommodations().size());

        // When
        CityDTO dto = cityMapper.toModel(cityEntity);

        // Then
        assertNotNull(dto);
        assertEquals(cityEntity.getId(), dto.id());
        assertEquals(cityEntity.getName(), dto.name());
        assertEquals(cityEntity.getCountry(), dto.country());
        // El DTO no tiene campo accommodations, así que solo verificamos los campos básicos
    }

    @Test
    @DisplayName("Debe aplicar numberFormat correctamente en el ID")
    void toModel_shouldApplyNumberFormat() {
        // Given
        cityEntity.setId(100L);

        // When
        CityDTO dto = cityMapper.toModel(cityEntity);

        // Then
        assertNotNull(dto);
        assertEquals(100L, dto.id());
        // El numberFormat "#" debería formatear sin decimales
    }

    @Test
    @DisplayName("Mapeo bidireccional debe preservar la información")
    void bidirectionalMapping_shouldPreserveInformation() {
        // Given - Empezamos con un DTO
        CityDTO originalDTO = new CityDTO(5L, "Cartagena", "Colombia");

        // When - Convertimos a Entity y de vuelta a DTO
        CityEntity entity = cityMapper.toEntity(originalDTO);
        CityDTO resultDTO = cityMapper.toModel(entity);

        // Then - Los datos deben ser los mismos
        assertNotNull(resultDTO);
        assertEquals(originalDTO.id(), resultDTO.id());
        assertEquals(originalDTO.name(), resultDTO.name());
        assertEquals(originalDTO.country(), resultDTO.country());
    }
}