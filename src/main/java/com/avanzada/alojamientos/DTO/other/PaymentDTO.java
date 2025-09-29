package com.avanzada.alojamientos.DTO.other;

import com.avanzada.alojamientos.DTO.model.PaymentMethod;
import com.avanzada.alojamientos.DTO.model.PaymentStatus;

import java.math.BigDecimal;

public record PaymentDTO(
        Long id,
        Long reservationId,
        BigDecimal amount,
        PaymentMethod method,
        PaymentStatus status,
        String paidAt
) {
}
