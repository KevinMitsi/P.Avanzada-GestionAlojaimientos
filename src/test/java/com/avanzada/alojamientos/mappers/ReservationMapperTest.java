package com.avanzada.alojamientos.mappers;

import com.avanzada.alojamientos.DTO.model.CancelledBy;
import com.avanzada.alojamientos.DTO.model.ReservationStatus;
import com.avanzada.alojamientos.DTO.reservation.CreateReservationDTO;
import com.avanzada.alojamientos.DTO.reservation.ReservationDTO;
import com.avanzada.alojamientos.entities.AccommodationEntity;
import com.avanzada.alojamientos.entities.ReservationEntity;
import com.avanzada.alojamientos.entities.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ReservationMapperTest {

    @Autowired
    private ReservationMapper reservationMapper;

    private ReservationEntity reservationEntity;
    private CreateReservationDTO createReservationDTO;
    private UserEntity hostEntity;

    @BeforeEach
    void setUp() {
        // Setup User Entity
        UserEntity userEntity = new UserEntity();
        userEntity.setId(100L);
        userEntity.setName("Cliente Test");
        userEntity.setEmail("cliente@example.com");

        // Setup Host Entity
        hostEntity = new UserEntity();
        hostEntity.setId(200L);
        hostEntity.setName("Anfitrión Test");
        hostEntity.setEmail("host@example.com");

        // Setup Accommodation Entity
        AccommodationEntity accommodationEntity = new AccommodationEntity();
        accommodationEntity.setId(50L);
        accommodationEntity.setTitle("Apartamento Test");
        accommodationEntity.setHost(hostEntity);

        // Setup Reservation Entity
        reservationEntity = new ReservationEntity();
        reservationEntity.setId(10L);
        reservationEntity.setStartDate(LocalDate.of(2025, 12, 15));
        reservationEntity.setEndDate(LocalDate.of(2025, 12, 20));
        reservationEntity.setNights(5);
        reservationEntity.setTotalPrice(new BigDecimal("1000000.00"));
        reservationEntity.setStatus(ReservationStatus.CONFIRMED);
        reservationEntity.setCreatedAt(LocalDateTime.of(2025, 10, 1, 10, 30, 0));
        reservationEntity.setUpdatedAt(LocalDateTime.of(2025, 10, 5, 15, 45, 30));
        reservationEntity.setUser(userEntity);
        reservationEntity.setAccommodation(accommodationEntity);
        reservationEntity.setCancelledAt(null);
        reservationEntity.setMotivoCancelacion(null);
        reservationEntity.setCancelledBy(null);

        // Setup CreateReservationDTO
        createReservationDTO = new CreateReservationDTO(
                50L,
                LocalDate.of(2025, 11, 1),
                LocalDate.of(2025, 11, 5),
                2
        );
    }

    @Test
    @DisplayName("Debe mapear ReservationEntity a ReservationDTO correctamente")
    void toDTO_shouldMapCorrectly() {
        // When
        ReservationDTO dto = reservationMapper.toDTO(reservationEntity);

        // Then
        assertNotNull(dto);
        assertEquals(reservationEntity.getId(), dto.id());
        assertEquals(reservationEntity.getAccommodation().getId(), dto.accommodationId());
        assertEquals(reservationEntity.getUser().getId(), dto.userId());
        assertEquals(reservationEntity.getAccommodation().getHost().getId(), dto.hostId());
        assertEquals(reservationEntity.getStartDate(), dto.startDate());
        assertEquals(reservationEntity.getEndDate(), dto.endDate());
        assertEquals(reservationEntity.getNights(), dto.nights());
        assertEquals(reservationEntity.getTotalPrice(), dto.totalPrice());
        assertEquals(reservationEntity.getStatus(), dto.status());

        // Verificar formato de fechas
        assertEquals("2025-10-01T10:30:00", dto.createdAt());
        assertEquals("2025-10-05T15:45:30", dto.updatedAt());
        assertNull(dto.canceladoAt());
        assertNull(dto.motivoCancelacion());
        assertNull(dto.canceladoPor());
    }

    @Test
    @DisplayName("Debe retornar null cuando ReservationEntity es null")
    void toDTO_withNullEntity_shouldReturnNull() {
        // When
        ReservationDTO dto = reservationMapper.toDTO(null);

        // Then
        assertNull(dto);
    }

    @Test
    @DisplayName("Debe mapear correctamente los diferentes estados de reserva")
    void toDTO_withDifferentStatuses_shouldMapCorrectly() {
        // Given - PENDING
        reservationEntity.setStatus(ReservationStatus.PENDING);
        ReservationDTO dto1 = reservationMapper.toDTO(reservationEntity);
        assertEquals(ReservationStatus.PENDING, dto1.status());

        // Given - CONFIRMED
        reservationEntity.setStatus(ReservationStatus.CONFIRMED);
        ReservationDTO dto2 = reservationMapper.toDTO(reservationEntity);
        assertEquals(ReservationStatus.CONFIRMED, dto2.status());

        // Given - CANCELLED
        reservationEntity.setStatus(ReservationStatus.CANCELLED);
        ReservationDTO dto3 = reservationMapper.toDTO(reservationEntity);
        assertEquals(ReservationStatus.CANCELLED, dto3.status());

        // Given - COMPLETED
        reservationEntity.setStatus(ReservationStatus.COMPLETED);
        ReservationDTO dto4 = reservationMapper.toDTO(reservationEntity);
        assertEquals(ReservationStatus.COMPLETED, dto4.status());
    }

    @Test
    @DisplayName("Debe mapear correctamente una reserva cancelada con todos los campos")
    void toDTO_withCancelledReservation_shouldMapCorrectly() {
        // Given
        reservationEntity.setStatus(ReservationStatus.CANCELLED);
        reservationEntity.setCancelledAt(LocalDateTime.of(2025, 10, 10, 9, 15, 45));
        reservationEntity.setMotivoCancelacion("El cliente decidió cancelar por cambio de planes");
        reservationEntity.setCancelledBy(CancelledBy.USER);

        // When
        ReservationDTO dto = reservationMapper.toDTO(reservationEntity);

        // Then
        assertNotNull(dto);
        assertEquals(ReservationStatus.CANCELLED, dto.status());
        assertEquals("2025-10-10T09:15:45", dto.canceladoAt());
        assertEquals("El cliente decidió cancelar por cambio de planes", dto.motivoCancelacion());
        assertEquals("USER", dto.canceladoPor());
    }

    @Test
    @DisplayName("Debe mapear correctamente canceladoPor desde el enum CancelledBy")
    void toDTO_shouldMapCancelledByEnumCorrectly() {
        // Given - Cancelado por USER
        reservationEntity.setCancelledBy(CancelledBy.USER);
        ReservationDTO dto1 = reservationMapper.toDTO(reservationEntity);
        assertEquals("USER", dto1.canceladoPor());

        // Given - Cancelado por HOST
        reservationEntity.setCancelledBy(CancelledBy.HOST);
        ReservationDTO dto2 = reservationMapper.toDTO(reservationEntity);
        assertEquals("HOST", dto2.canceladoPor());

        // Given - Cancelado por SYSTEM
        reservationEntity.setCancelledBy(CancelledBy.SYSTEM);
        ReservationDTO dto3 = reservationMapper.toDTO(reservationEntity);
        assertEquals("SYSTEM", dto3.canceladoPor());

        // Given - Sin cancelar
        reservationEntity.setCancelledBy(null);
        ReservationDTO dto4 = reservationMapper.toDTO(reservationEntity);
        assertNull(dto4.canceladoPor());
    }

    @Test
    @DisplayName("Debe mapear CreateReservationDTO a ReservationEntity correctamente")
    void toEntity_shouldMapCorrectly() {
        // When
        ReservationEntity entity = reservationMapper.toEntity(createReservationDTO);

        // Then
        assertNotNull(entity);
        assertEquals(createReservationDTO.startDate(), entity.getStartDate());
        assertEquals(createReservationDTO.endDate(), entity.getEndDate());

        // Verificar campos ignorados (se asignan en el servicio)
        assertNull(entity.getId());
        assertNull(entity.getNights());
        assertNull(entity.getTotalPrice());
        assertNull(entity.getStatus());
        assertNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());
        assertNull(entity.getCancelledAt());
        assertNull(entity.getMotivoCancelacion());
        assertNull(entity.getCancelledBy());
        assertNull(entity.getAccommodation());
        assertNull(entity.getUser());
        assertNull(entity.getPayment());
        assertNull(entity.getComment());
    }

    @Test
    @DisplayName("Debe retornar null cuando CreateReservationDTO es null")
    void toEntity_withNullDTO_shouldReturnNull() {
        // When
        ReservationEntity entity = reservationMapper.toEntity(null);

        // Then
        assertNull(entity);
    }

    @Test
    @DisplayName("Debe formatear correctamente diferentes fechas en formato ISO-8601")
    void toDTO_shouldFormatDatesCorrectly() {
        // Given - Fecha de medianoche
        reservationEntity.setCreatedAt(LocalDateTime.of(2025, 1, 1, 0, 0, 0));
        reservationEntity.setUpdatedAt(null);

        ReservationDTO dto1 = reservationMapper.toDTO(reservationEntity);
        assertEquals("2025-01-01T00:00:00", dto1.createdAt());
        assertNull(dto1.updatedAt());

        // Given - Fecha de fin de día
        reservationEntity.setCreatedAt(LocalDateTime.of(2025, 12, 31, 23, 59, 59));
        reservationEntity.setUpdatedAt(LocalDateTime.of(2025, 12, 31, 23, 59, 59));

        ReservationDTO dto2 = reservationMapper.toDTO(reservationEntity);
        assertEquals("2025-12-31T23:59:59", dto2.createdAt());
        assertEquals("2025-12-31T23:59:59", dto2.updatedAt());
    }

    @Test
    @DisplayName("Debe mapear correctamente el hostId desde accommodation.host.id")
    void toDTO_shouldMapHostIdCorrectly() {
        // Given - Diferentes hosts
        hostEntity.setId(1L);
        ReservationDTO dto1 = reservationMapper.toDTO(reservationEntity);
        assertEquals(1L, dto1.hostId());

        hostEntity.setId(9999L);
        ReservationDTO dto2 = reservationMapper.toDTO(reservationEntity);
        assertEquals(9999L, dto2.hostId());
    }

    @Test
    @DisplayName("Debe manejar reservas de diferentes duraciones correctamente")
    void toDTO_withDifferentDurations_shouldMapCorrectly() {
        // Given - 1 noche
        reservationEntity.setStartDate(LocalDate.of(2025, 11, 1));
        reservationEntity.setEndDate(LocalDate.of(2025, 11, 2));
        reservationEntity.setNights(1);

        ReservationDTO dto1 = reservationMapper.toDTO(reservationEntity);
        assertEquals(1, dto1.nights());
        assertEquals(LocalDate.of(2025, 11, 1), dto1.startDate());
        assertEquals(LocalDate.of(2025, 11, 2), dto1.endDate());

        // Given - 30 noches
        reservationEntity.setStartDate(LocalDate.of(2025, 11, 1));
        reservationEntity.setEndDate(LocalDate.of(2025, 12, 1));
        reservationEntity.setNights(30);

        ReservationDTO dto2 = reservationMapper.toDTO(reservationEntity);
        assertEquals(30, dto2.nights());
    }

    @Test
    @DisplayName("Debe manejar diferentes precios correctamente")
    void toDTO_withDifferentPrices_shouldMapCorrectly() {
        // Given - Precio bajo
        reservationEntity.setTotalPrice(new BigDecimal("100000.00"));
        ReservationDTO dto1 = reservationMapper.toDTO(reservationEntity);
        assertEquals(new BigDecimal("100000.00"), dto1.totalPrice());

        // Given - Precio alto
        reservationEntity.setTotalPrice(new BigDecimal("9999999.99"));
        ReservationDTO dto2 = reservationMapper.toDTO(reservationEntity);
        assertEquals(new BigDecimal("9999999.99"), dto2.totalPrice());
    }

    @Test
    @DisplayName("Debe manejar updatedAt null correctamente")
    void toDTO_withNullUpdatedAt_shouldMapCorrectly() {
        // Given
        reservationEntity.setUpdatedAt(null);

        // When
        ReservationDTO dto = reservationMapper.toDTO(reservationEntity);

        // Then
        assertNotNull(dto);
        assertNull(dto.updatedAt());
    }

    @Test
    @DisplayName("Debe ignorar todos los campos especificados al crear entidad desde DTO")
    void toEntity_shouldIgnoreAllSpecifiedFields() {
        // When
        ReservationEntity entity = reservationMapper.toEntity(createReservationDTO);

        // Then - Verificar que todos los campos ignorados son null
        assertNull(entity.getId());
        assertNull(entity.getNights());
        assertNull(entity.getTotalPrice());
        assertNull(entity.getStatus());
        assertNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());
        assertNull(entity.getCancelledAt());
        assertNull(entity.getMotivoCancelacion());
        assertNull(entity.getCancelledBy());
        assertNull(entity.getAccommodation());
        assertNull(entity.getUser());
        assertNull(entity.getPayment());
        assertNull(entity.getComment());
    }

    @Test
    @DisplayName("Debe mantener consistencia en múltiples llamadas con los mismos datos")
    void toDTO_shouldBeConsistentAcrossMultipleCalls() {
        // When - Llamar múltiples veces
        ReservationDTO dto1 = reservationMapper.toDTO(reservationEntity);
        ReservationDTO dto2 = reservationMapper.toDTO(reservationEntity);

        // Then - Los resultados deben ser iguales
        assertNotNull(dto1);
        assertNotNull(dto2);
        assertEquals(dto1.id(), dto2.id());
        assertEquals(dto1.accommodationId(), dto2.accommodationId());
        assertEquals(dto1.userId(), dto2.userId());
        assertEquals(dto1.hostId(), dto2.hostId());
        assertEquals(dto1.startDate(), dto2.startDate());
        assertEquals(dto1.endDate(), dto2.endDate());
        assertEquals(dto1.nights(), dto2.nights());
        assertEquals(dto1.totalPrice(), dto2.totalPrice());
        assertEquals(dto1.status(), dto2.status());
        assertEquals(dto1.createdAt(), dto2.createdAt());
        assertEquals(dto1.updatedAt(), dto2.updatedAt());
    }

    @Test
    @DisplayName("Debe mapear solo startDate y endDate desde CreateReservationDTO")
    void toEntity_shouldMapOnlyDates() {
        // Given - DTO con todas las fechas
        CreateReservationDTO dto = new CreateReservationDTO(
                100L,
                LocalDate.of(2025, 12, 25),
                LocalDate.of(2025, 12, 30),
                4
        );

        // When
        ReservationEntity entity = reservationMapper.toEntity(dto);

        // Then - Solo las fechas deben estar mapeadas
        assertNotNull(entity);
        assertEquals(LocalDate.of(2025, 12, 25), entity.getStartDate());
        assertEquals(LocalDate.of(2025, 12, 30), entity.getEndDate());

        // El resto debe ser null (incluyendo nights que se calcula con @PrePersist)
        assertNull(entity.getNights());
    }

    @Test
    @DisplayName("Debe aplicar numberFormat correctamente en los IDs")
    void toDTO_shouldApplyNumberFormatToIds() {
        // When
        ReservationDTO dto = reservationMapper.toDTO(reservationEntity);

        // Then - Verificar que los IDs se formatean correctamente
        assertNotNull(dto.id());
        assertNotNull(dto.accommodationId());
        assertNotNull(dto.userId());
        assertNotNull(dto.hostId());

        assertEquals(10L, dto.id());
        assertEquals(50L, dto.accommodationId());
        assertEquals(100L, dto.userId());
        assertEquals(200L, dto.hostId());
    }

    @Test
    @DisplayName("Debe mapear motivo de cancelación largo correctamente")
    void toDTO_withLongCancellationReason_shouldMapCorrectly() {
        // Given
        String longReason = "Este es un motivo de cancelación muy largo que explica detalladamente por qué se canceló la reserva. ".repeat(5);
        reservationEntity.setMotivoCancelacion(longReason);

        // When
        ReservationDTO dto = reservationMapper.toDTO(reservationEntity);

        // Then
        assertNotNull(dto);
        assertEquals(longReason, dto.motivoCancelacion());
    }

    @Test
    @DisplayName("Debe manejar reservas con fechas en el pasado y futuro")
    void toDTO_withDifferentDateRanges_shouldMapCorrectly() {
        // Given - Reserva en el pasado
        reservationEntity.setStartDate(LocalDate.of(2020, 1, 1));
        reservationEntity.setEndDate(LocalDate.of(2020, 1, 5));

        ReservationDTO dto1 = reservationMapper.toDTO(reservationEntity);
        assertEquals(LocalDate.of(2020, 1, 1), dto1.startDate());
        assertEquals(LocalDate.of(2020, 1, 5), dto1.endDate());

        // Given - Reserva en el futuro
        reservationEntity.setStartDate(LocalDate.of(2030, 12, 1));
        reservationEntity.setEndDate(LocalDate.of(2030, 12, 10));

        ReservationDTO dto2 = reservationMapper.toDTO(reservationEntity);
        assertEquals(LocalDate.of(2030, 12, 1), dto2.startDate());
        assertEquals(LocalDate.of(2030, 12, 10), dto2.endDate());
    }
}

