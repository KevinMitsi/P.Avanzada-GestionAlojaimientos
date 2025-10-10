package com.avanzada.alojamientos.mappers;

import com.avanzada.alojamientos.DTO.model.PaymentMethod;
import com.avanzada.alojamientos.DTO.model.PaymentStatus;
import com.avanzada.alojamientos.DTO.other.PaymentDTO;
import com.avanzada.alojamientos.entities.PaymentEntity;
import com.avanzada.alojamientos.entities.ReservationEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class PaymentMapperTest {

    private PaymentMapper paymentMapper;
    private PaymentEntity paymentEntity;
    private PaymentDTO paymentDTO;
    private ReservationEntity reservationEntity;

    @BeforeEach
    void setUp() {
        paymentMapper = Mappers.getMapper(PaymentMapper.class);

        reservationEntity = new ReservationEntity();
        reservationEntity.setId(10L);

        paymentEntity = new PaymentEntity();
        paymentEntity.setId(1L);
        paymentEntity.setAmount(new BigDecimal("250000.50"));
        paymentEntity.setMethod(PaymentMethod.CARD);
        paymentEntity.setStatus(PaymentStatus.COMPLETED);
        paymentEntity.setPaidAt(LocalDateTime.of(2025, 10, 10, 14, 30, 0));
        paymentEntity.setReservation(reservationEntity);

        paymentDTO = new PaymentDTO(
                1L,
                10L,
                new BigDecimal("250000.50"),
                PaymentMethod.CARD,
                PaymentStatus.COMPLETED,
                "2025-10-10T14:30:00"
        );
    }

    @Test
    @DisplayName("Debe mapear PaymentEntity a PaymentDTO correctamente")
    void toDTO_shouldMapCorrectly() {
        PaymentDTO dto = paymentMapper.toDTO(paymentEntity);

        assertNotNull(dto);
        assertEquals(paymentEntity.getId(), dto.id());
        assertEquals(paymentEntity.getReservation().getId(), dto.reservationId());
        assertEquals(paymentEntity.getAmount(), dto.amount());
        assertEquals(paymentEntity.getMethod(), dto.method());
        assertEquals(paymentEntity.getStatus(), dto.status());

        String formattedDate = paymentEntity.getPaidAt()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        assertEquals(formattedDate, dto.paidAt());
    }

    @Test
    @DisplayName("Debe retornar null cuando PaymentEntity es null")
    void toDTO_withNullEntity_shouldReturnNull() {
        PaymentDTO dto = paymentMapper.toDTO(null);
        assertNull(dto);
    }

    @Test
    @DisplayName("Debe mapear PaymentDTO a PaymentEntity correctamente")
    void toEntity_shouldMapCorrectly() {
        PaymentEntity entity = paymentMapper.toEntity(paymentDTO);

        assertNotNull(entity);
        assertNull(entity.getId()); // Ignorado
        assertNull(entity.getReservation()); // Ignorado
        assertNull(entity.getPaidAt()); // Ignorado
        assertEquals(paymentDTO.amount(), entity.getAmount());
        assertEquals(paymentDTO.method(), entity.getMethod());
        assertEquals(paymentDTO.status(), entity.getStatus());
    }

    @Test
    @DisplayName("Debe retornar null cuando PaymentDTO es null")
    void toEntity_withNullDTO_shouldReturnNull() {
        PaymentEntity entity = paymentMapper.toEntity(null);
        assertNull(entity);
    }

    @Test
    @DisplayName("Debe manejar correctamente fecha nula en PaymentEntity (paidAt null)")
    void toDTO_withNullPaidAt_shouldHandleCorrectly() {
        paymentEntity.setPaidAt(null);
        PaymentDTO dto = paymentMapper.toDTO(paymentEntity);
        assertNotNull(dto);
        assertNull(dto.paidAt());
    }

    @Test
    @DisplayName("Debe manejar correctamente reservation nula en PaymentEntity")
    void toDTO_withNullReservation_shouldHandleCorrectly() {
        paymentEntity.setReservation(null);
        PaymentDTO dto = paymentMapper.toDTO(paymentEntity);
        assertNotNull(dto);
        assertNull(dto.reservationId());
    }
}
