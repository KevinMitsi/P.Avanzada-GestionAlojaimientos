//package com.avanzada.alojamientos.repositories;
//
//import com.avanzada.alojamientos.entities.ReservationEntity;
//import com.avanzada.alojamientos.DTO.model.ReservationStatus;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//
//import java.time.LocalDate;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@DataJpaTest
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//class ReservationRepositoryTest {
//
//    @Autowired
//    private ReservationRepository reservationRepository;
//
//    @Test
//    void testFindById_ShouldReturnReservation() {
//        // Given: ID de reservación existente
//        Long reservationId = 1L;
//
//        // When: Buscar por ID
//        Optional<ReservationEntity> result = reservationRepository.findById(reservationId);
//
//        // Then: Debe encontrar la reservación
//        assertTrue(result.isPresent(), "La reservación debe existir");
//        ReservationEntity reservation = result.get();
//
//        assertNotNull(reservation.getAccommodation(), "El alojamiento debe estar cargado");
//        assertNotNull(reservation.getUser(), "El usuario debe estar cargado");
//        assertEquals(ReservationStatus.COMPLETED, reservation.getStatus());
//    }
//
//    @Test
//    void testFindByUserIdAndStatus() {
//        // Given: Usuario con reservaciones completadas
//        Long userId = 5L; // Laura Pérez
//        ReservationStatus status = ReservationStatus.COMPLETED;
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // When: Buscar reservaciones por usuario y estado
//        Page<ReservationEntity> result = reservationRepository.findByUserIdAndStatus(
//                userId, status, pageable
//        );
//
//        // Then: Debe encontrar al menos una reservación
//        assertFalse(result.isEmpty(), "Laura debe tener reservaciones completadas");
//        assertTrue(result.getContent().stream()
//                .allMatch(r -> r.getUser().getId().equals(userId)
//                        && r.getStatus().equals(status)),
//                "Todas deben ser de Laura y estar completadas");
//    }
//
//    @Test
//    void testFindByUserId() {
//        // Given: Usuario con reservaciones
//        Long userId = 5L; // Laura Pérez
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // When: Buscar todas las reservaciones del usuario
//        Page<ReservationEntity> result = reservationRepository.findByUserId(userId, pageable);
//
//        // Then: Debe encontrar al menos 2 reservaciones
//        assertTrue(result.getTotalElements() >= 2, "Laura debe tener al menos 2 reservaciones");
//        assertTrue(result.getContent().stream()
//                .allMatch(r -> r.getUser().getId().equals(userId)),
//                "Todas las reservaciones deben ser de Laura");
//    }
//
//    @Test
//    void testFindByAccommodationHostIdAndStatus() {
//        // Given: Host con reservaciones confirmadas
//        Long hostId = 4L; // Ana Martínez (alojamiento 3)
//        ReservationStatus status = ReservationStatus.CONFIRMED;
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // When: Buscar reservaciones del host por estado
//        Page<ReservationEntity> result = reservationRepository.findByAccommodationHostIdAndStatus(
//                hostId, status, pageable
//        );
//
//        // Then: Debe encontrar reservaciones confirmadas
//        assertFalse(result.isEmpty(), "El host debe tener reservaciones confirmadas");
//        assertTrue(result.getContent().stream()
//                .allMatch(r -> r.getStatus().equals(status)),
//                "Todas deben estar confirmadas");
//    }
//
//    @Test
//    void testFindByAccommodationHostId() {
//        // Given: Host con reservaciones
//        Long hostId = 2L; // María González (alojamiento 1)
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // When: Buscar todas las reservaciones del host
//        Page<ReservationEntity> result = reservationRepository.findByAccommodationHostId(
//                hostId, pageable
//        );
//
//        // Then: Debe encontrar reservaciones
//        assertFalse(result.isEmpty(), "María debe tener reservaciones en sus alojamientos");
//    }
//
//    @Test
//    void testFindByAccommodationId() {
//        // Given: Alojamiento con reservaciones
//        Long accommodationId = 1L;
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // When: Buscar reservaciones del alojamiento
//        Page<ReservationEntity> result = reservationRepository.findByAccommodationId(
//                accommodationId, pageable
//        );
//
//        // Then: Debe encontrar al menos 2 reservaciones
//        assertTrue(result.getTotalElements() >= 2,
//                "El alojamiento 1 debe tener al menos 2 reservaciones");
//        assertTrue(result.getContent().stream()
//                .allMatch(r -> r.getAccommodation().getId().equals(accommodationId)),
//                "Todas las reservaciones deben ser del alojamiento 1");
//    }
//
//    @Test
//    void testExistsOverlappingReservation_ShouldReturnTrue() {
//        // Given: Fechas que se solapan con una reservación existente
//        Long accommodationId = 1L;
//        LocalDate startDate = LocalDate.of(2024, 3, 3); // Solapa con 2024-03-01 a 2024-03-05
//        LocalDate endDate = LocalDate.of(2024, 3, 7);
//
//        // When: Verificar solapamiento
//        boolean exists = reservationRepository.existsOverlappingReservation(
//                accommodationId, startDate, endDate, null
//        );
//
//        // Then: Debe encontrar solapamiento
//        assertTrue(exists, "Debe existir una reservación que se solape");
//    }
//
//    @Test
//    void testExistsOverlappingReservation_ShouldReturnFalse() {
//        // Given: Fechas que NO se solapan con ninguna reservación
//        Long accommodationId = 1L;
//        LocalDate startDate = LocalDate.of(2025, 6, 1);
//        LocalDate endDate = LocalDate.of(2025, 6, 5);
//
//        // When: Verificar solapamiento
//        boolean exists = reservationRepository.existsOverlappingReservation(
//                accommodationId, startDate, endDate, null
//        );
//
//        // Then: No debe encontrar solapamiento
//        assertFalse(exists, "No debe existir una reservación que se solape");
//    }
//
//    @Test
//    void testExistsOverlappingReservation_ExcludingCurrentReservation() {
//        // Given: Actualización de una reservación existente
//        Long accommodationId = 1L;
//        Long currentReservationId = 1L;
//        LocalDate startDate = LocalDate.of(2024, 3, 1);
//        LocalDate endDate = LocalDate.of(2024, 3, 5);
//
//        // When: Verificar solapamiento excluyendo la reservación actual
//        boolean exists = reservationRepository.existsOverlappingReservation(
//                accommodationId, startDate, endDate, currentReservationId
//        );
//
//        // Then: No debe encontrar solapamiento (excluye la misma reservación)
//        assertFalse(exists, "No debe encontrar solapamiento al excluir la misma reservación");
//    }
//
//    @Test
//    void testFindOverlappingReservations() {
//        // Given: Fechas que se solapan
//        Long accommodationId = 1L;
//        LocalDate startDate = LocalDate.of(2024, 3, 3);
//        LocalDate endDate = LocalDate.of(2024, 3, 7);
//
//        // When: Buscar reservaciones solapadas
//        List<ReservationEntity> result = reservationRepository.findOverlappingReservations(
//                accommodationId, startDate, endDate, null
//        );
//
//        // Then: Debe encontrar al menos una reservación solapada
//        assertFalse(result.isEmpty(), "Debe encontrar reservaciones solapadas");
//        assertTrue(result.stream()
//                .anyMatch(r -> r.getStatus() != ReservationStatus.CANCELLED),
//                "Debe incluir reservaciones no canceladas");
//    }
//
//    @Test
//    void testCountByUserIdAndStatus() {
//        // Given: Usuario con reservaciones completadas
//        Long userId = 5L; // Laura Pérez
//        ReservationStatus status = ReservationStatus.COMPLETED;
//
//        // When: Contar reservaciones
//        long count = reservationRepository.countByUserIdAndStatus(userId, status);
//
//        // Then: Debe tener al menos 1 reservación completada
//        assertTrue(count >= 1, "Laura debe tener al menos 1 reservación completada");
//    }
//
//    @Test
//    void testFindUpcomingReservations() {
//        // Given: Fecha de referencia en el pasado para ver reservaciones "futuras" de los datos
//        LocalDate fromDate = LocalDate.of(2024, 3, 1);
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // When: Buscar reservaciones próximas
//        Page<ReservationEntity> result = reservationRepository.findUpcomingReservations(
//                fromDate, pageable
//        );
//
//        // Then: Debe encontrar reservaciones futuras no canceladas
//        assertFalse(result.isEmpty(), "Debe haber reservaciones próximas");
//        assertTrue(result.getContent().stream()
//                .allMatch(r -> r.getStartDate().isAfter(fromDate) || r.getStartDate().isEqual(fromDate)),
//                "Todas deben ser desde la fecha especificada");
//    }
//
//    @Test
//    void testCancelledReservation() {
//        // Given: ID de reservación cancelada
//        Long reservationId = 4L; // Reservación cancelada
//
//        // When: Buscar la reservación
//        Optional<ReservationEntity> result = reservationRepository.findById(reservationId);
//
//        // Then: Debe estar cancelada
//        assertTrue(result.isPresent(), "La reservación debe existir");
//        assertEquals(ReservationStatus.CANCELLED, result.get().getStatus());
//        assertNotNull(result.get().getCancelledAt(), "Debe tener fecha de cancelación");
//        assertNotNull(result.get().getMotivoCancelacion(), "Debe tener motivo de cancelación");
//        assertEquals("USER", result.get().getCancelledBy(), "Debe indicar quién canceló");
//    }
//
//    @Test
//    void testReservationWithPayment() {
//        // Given: Reservación con pago completado
//        Long reservationId = 1L;
//
//        // When: Buscar la reservación
//        Optional<ReservationEntity> result = reservationRepository.findById(reservationId);
//
//        // Then: Debe tener pago asociado
//        assertTrue(result.isPresent(), "La reservación debe existir");
//        assertNotNull(result.get().getPayment(), "Debe tener pago asociado");
//        assertEquals("COMPLETED", result.get().getPayment().getStatus().toString());
//    }
//
//    @Test
//    void testFindByIdWithPayment() {
//        // Given: ID de reservación con pago
//        Long reservationId = 1L;
//
//        // When: Buscar con pago
//        Optional<ReservationEntity> result = reservationRepository.findByIdWithPayment(reservationId);
//
//        // Then: Debe tener pago cargado
//        assertTrue(result.isPresent(), "La reservación debe existir");
//        assertNotNull(result.get().getPayment(), "El pago debe estar cargado");
//    }
//
//    @Test
//    void testReservationDates() {
//        // Given: Reservación con fechas específicas
//        Long reservationId = 1L;
//
//        // When: Buscar la reservación
//        Optional<ReservationEntity> result = reservationRepository.findById(reservationId);
//
//        // Then: Verificar fechas y noches
//        assertTrue(result.isPresent(), "La reservación debe existir");
//        ReservationEntity reservation = result.get();
//
//        assertEquals(LocalDate.of(2024, 3, 1), reservation.getStartDate());
//        assertEquals(LocalDate.of(2024, 3, 5), reservation.getEndDate());
//        assertEquals(4, reservation.getNights(), "Debe ser 4 noches");
//    }
//}
//
