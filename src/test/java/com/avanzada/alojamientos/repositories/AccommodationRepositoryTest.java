package com.avanzada.alojamientos.repositories;

import com.avanzada.alojamientos.entities.AccommodationEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AccommodationRepositoryTest {

    @Autowired
    private AccommodationRepository accommodationRepository;

    @Test
    void testFindById_ShouldReturnAccommodationWithRelations() {
        // Given: ID de alojamiento existente en import.sql
        Long accommodationId = 1L;

        // When: Buscar por ID
        Optional<AccommodationEntity> result = accommodationRepository.findById(accommodationId);

        // Then: Verificar que se encontró y tiene relaciones cargadas
        assertTrue(result.isPresent(), "El alojamiento debe existir");
        AccommodationEntity accommodation = result.get();

        assertEquals("Apartamento Moderno en El Poblado", accommodation.getTitle());
        assertNotNull(accommodation.getHost(), "El host debe estar cargado");
        assertNotNull(accommodation.getCity(), "La ciudad debe estar cargada");
        assertNotNull(accommodation.getImages(), "Las imágenes deben estar cargadas");
        assertNotNull(accommodation.getServices(), "Los servicios deben estar cargados");

        assertFalse(accommodation.getImages().isEmpty(), "Debe tener al menos una imagen");
        assertFalse(accommodation.getServices().isEmpty(), "Debe tener al menos un servicio");
    }

    @Test
    void testExistsByIdAndSoftDeletedFalse_ShouldReturnTrue() {
        // Given: ID de alojamiento existente y no eliminado
        Long accommodationId = 1L;

        // When: Verificar existencia
        Boolean exists = accommodationRepository.existsByIdAndSoftDeletedFalse(accommodationId);

        // Then: Debe retornar true
        assertTrue(exists, "El alojamiento debe existir y no estar eliminado");
    }

    @Test
    void testSearch_WithCityFilter() {
        // Given: Filtro por ciudad de Medellín (ID = 1)
        Long cityId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        // When: Buscar alojamientos en Medellín
        Page<AccommodationEntity> result = accommodationRepository.search(
                cityId, null, null, null, null, null, pageable
        );

        // Then: Debe encontrar al menos 1 alojamiento
        assertFalse(result.isEmpty(), "Debe encontrar alojamientos en Medellín");
        assertTrue(result.getContent().stream()
                .allMatch(a -> a.getCity().getId().equals(cityId)),
                "Todos los alojamientos deben ser de Medellín");
    }

    @Test
    void testSearch_WithPriceRange() {
        // Given: Rango de precios entre 80000 y 150000
        BigDecimal minPrice = new BigDecimal("80000.00");
        BigDecimal maxPrice = new BigDecimal("150000.00");
        Pageable pageable = PageRequest.of(0, 10);

        // When: Buscar alojamientos en ese rango
        Page<AccommodationEntity> result = accommodationRepository.search(
                null, minPrice, maxPrice, null, null, null, pageable
        );

        // Then: Todos deben estar en el rango
        assertFalse(result.isEmpty(), "Debe encontrar alojamientos en el rango de precio");
        assertTrue(result.getContent().stream()
                .allMatch(a -> a.getPricePerNight().compareTo(minPrice) >= 0
                        && a.getPricePerNight().compareTo(maxPrice) <= 0),
                "Todos los alojamientos deben estar en el rango de precio");
    }

    @Test
    void testSearch_WithGuestsFilter() {
        // Given: Capacidad mínima de 4 huéspedes
        Integer minGuests = 4;
        Pageable pageable = PageRequest.of(0, 10);

        // When: Buscar alojamientos con esa capacidad
        Page<AccommodationEntity> result = accommodationRepository.search(
                null, null, null, minGuests, null, null, pageable
        );

        // Then: Todos deben tener capacidad >= 4
        assertFalse(result.isEmpty(), "Debe encontrar alojamientos con capacidad para 4+ huéspedes");
        assertTrue(result.getContent().stream()
                .allMatch(a -> a.getMaxGuests() >= minGuests),
                "Todos los alojamientos deben tener capacidad para al menos 4 huéspedes");
    }

    @Test
    void testSearch_WithDateAvailability() {
        // Given: Fechas que NO se solapan con reservas existentes
        LocalDate startDate = LocalDate.of(2025, 6, 1);
        LocalDate endDate = LocalDate.of(2025, 6, 5);
        Pageable pageable = PageRequest.of(0, 10);

        // When: Buscar alojamientos disponibles
        Page<AccommodationEntity> result = accommodationRepository.search(
                null, null, null, null, startDate, endDate, pageable
        );

        // Then: Debe encontrar alojamientos disponibles
        assertFalse(result.isEmpty(), "Debe encontrar alojamientos disponibles en esas fechas");
    }

    @Test
    void testSearch_WithDateConflict() {
        // Given: Fechas que se solapan con una reserva existente (2024-03-01 a 2024-03-05)
        LocalDate startDate = LocalDate.of(2024, 3, 3);
        LocalDate endDate = LocalDate.of(2024, 3, 7);
        Pageable pageable = PageRequest.of(0, 10);

        // When: Buscar alojamientos
        Page<AccommodationEntity> result = accommodationRepository.search(
                null, null, null, null, startDate, endDate, pageable
        );

        // Then: El alojamiento 1 NO debe aparecer porque tiene una reserva
        assertTrue(result.getContent().stream()
                .noneMatch(a -> a.getId().equals(1L)),
                "El alojamiento 1 no debe estar disponible en esas fechas");
    }

    @Test
    void testFindAccommodationIdsWithServices() {
        // Given: Buscar alojamientos con WiFi y Aire acondicionado
        List<String> services = Arrays.asList("WiFi gratuito", "Aire acondicionado");
        Pageable pageable = PageRequest.of(0, 10);

        // When: Buscar IDs de alojamientos con esos servicios
        List<Long> result = accommodationRepository.findAccommodationIdsWithServices(
                null, null, null, null, null, null,
                services, services.size(), pageable
        );

        // Then: El alojamiento 1 debe estar en los resultados
        assertFalse(result.isEmpty(), "Debe encontrar alojamientos con esos servicios");
        assertTrue(result.contains(1L), "El alojamiento 1 tiene ambos servicios");
    }

    @Test
    void testFindByIdsWithEntityGraph() {
        // Given: Lista de IDs existentes
        List<Long> ids = Arrays.asList(1L, 2L, 3L);

        // When: Buscar por IDs
        List<AccommodationEntity> result = accommodationRepository.findByIdsWithEntityGraph(ids);

        // Then: Debe retornar los 3 alojamientos con sus relaciones cargadas
        assertEquals(3, result.size(), "Debe encontrar los 3 alojamientos");
        assertTrue(result.stream().allMatch(a -> a.getImages() != null),
                "Todas las imágenes deben estar cargadas");
        assertTrue(result.stream().allMatch(a -> a.getHost() != null),
                "Todos los hosts deben estar cargados");
    }

    @Test
    void testFindByHostIdAndSoftDeletedFalse() {
        // Given: ID del host María González (ID = 2)
        Long hostId = 2L;
        Pageable pageable = PageRequest.of(0, 10);

        // When: Buscar alojamientos de ese host
        Page<AccommodationEntity> result = accommodationRepository.findByHostIdAndSoftDeletedFalse(
                hostId, pageable
        );

        // Then: Debe encontrar al menos 1 alojamiento
        assertFalse(result.isEmpty(), "María debe tener al menos un alojamiento");
        assertTrue(result.getContent().stream()
                .allMatch(a -> a.getHost().getId().equals(hostId)),
                "Todos los alojamientos deben pertenecer a María");
    }

    @Test
    void testFindAll_WithPagination() {
        // Given: Paginación
        Pageable pageable = PageRequest.of(0, 10);

        // When: Buscar todos los alojamientos
        Page<AccommodationEntity> result = accommodationRepository.findAll(pageable);

        // Then: Debe retornar alojamientos
        assertFalse(result.isEmpty(), "Debe haber alojamientos en la base de datos");
        assertEquals(3, result.getTotalElements(), "Debe haber 3 alojamientos en total");
    }

    @Test
    void testFindByIdWithReservations() {
        // Given: ID de alojamiento con reservaciones
        Long accommodationId = 1L;

        // When: Buscar con reservaciones
        Optional<AccommodationEntity> result = accommodationRepository.findByIdWithReservations(
                accommodationId
        );

        // Then: Debe tener reservaciones cargadas
        assertTrue(result.isPresent(), "El alojamiento debe existir");
        assertNotNull(result.get().getReservations(), "Las reservaciones deben estar cargadas");
    }

    @Test
    void testFindByIdWithComments() {
        // Given: ID de alojamiento con comentarios
        Long accommodationId = 1L;

        // When: Buscar con comentarios
        Optional<AccommodationEntity> result = accommodationRepository.findByIdWithComments(
                accommodationId
        );

        // Then: Debe tener comentarios cargados
        assertTrue(result.isPresent(), "El alojamiento debe existir");
        assertNotNull(result.get().getComments(), "Los comentarios deben estar cargados");
        assertFalse(result.get().getComments().isEmpty(), "Debe tener al menos un comentario");
    }

    @Test
    void testCountFutureNonCancelledReservations() {
        // Given: ID de alojamiento y fecha actual
        Long accommodationId = 3L; // Tiene una reserva confirmada para abril 2024
        LocalDate fromDate = LocalDate.of(2024, 3, 1);

        // When: Contar reservas futuras
        long count = accommodationRepository.countFutureNonCancelledReservations(
                accommodationId, fromDate
        );

        // Then: Debe tener al menos 1 reserva futura
        assertTrue(count > 0, "El alojamiento 3 debe tener reservas futuras");
    }

    @Test
    void testCountFutureNonCancelledReservations_WithNoFutureReservations() {
        // Given: ID de alojamiento y fecha muy futura
        Long accommodationId = 1L;
        LocalDate fromDate = LocalDate.of(2025, 12, 31);

        // When: Contar reservas futuras
        long count = accommodationRepository.countFutureNonCancelledReservations(
                accommodationId, fromDate
        );

        // Then: No debe tener reservas futuras
        assertEquals(0, count, "No debe haber reservas futuras después de 2025");
    }

    @Test
    void testSearch_CombinedFilters() {
        // Given: Múltiples filtros combinados
        Long cityId = 2L; // Bogotá
        BigDecimal minPrice = new BigDecimal("50000.00");
        BigDecimal maxPrice = new BigDecimal("200000.00");
        Integer minGuests = 2;
        Pageable pageable = PageRequest.of(0, 10);

        // When: Buscar con todos los filtros
        Page<AccommodationEntity> result = accommodationRepository.search(
                cityId, minPrice, maxPrice, minGuests, null, null, pageable
        );

        // Then: Verificar que cumple todos los criterios
        assertFalse(result.isEmpty(), "Debe encontrar alojamientos que cumplan todos los criterios");
        assertTrue(result.getContent().stream()
                .allMatch(a -> a.getCity().getId().equals(cityId)
                        && a.getPricePerNight().compareTo(minPrice) >= 0
                        && a.getPricePerNight().compareTo(maxPrice) <= 0
                        && a.getMaxGuests() >= minGuests),
                "Todos los alojamientos deben cumplir todos los filtros");
    }
}