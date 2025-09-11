package com.avanzada.alojamientos.DTO;

import java.math.BigDecimal;

public record PaymentDTO(
        String id,
        String reservationId,
        BigDecimal amount,
        PaymentMethod method,
        PaymentStatus status,
        String paidAt
) {
}
