package com.avanzada.alojamientos.DTO;

import com.avanzada.alojamientos.DTO.model.PaymentMethod;
import com.avanzada.alojamientos.DTO.model.PaymentStatus;

import java.math.BigDecimal;

public record PaymentDTO(
        Long id,
        long reservationId,
        BigDecimal amount,
        PaymentMethod method,
        PaymentStatus status,
        String paidAt
) {
}
