package com.avanzada.alojamientos.DTO.reservation;



import com.avanzada.alojamientos.DTO.model.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReservationDTO(
        Long id,
        Long accommodationId,
        Long userId,
        Long hostId,
        LocalDate startDate,
        LocalDate endDate,
        Integer nights,
        BigDecimal totalPrice,
        ReservationStatus status,
        String createdAt,
        String updatedAt,
        String canceladoAt,
        String motivoCancelacion,
        String canceladoPor


) {
}