package com.avanzada.alojamientos.DTO.other;

import java.math.BigDecimal;

public record MercadoPagoPreferenceDTO(
        Long reservationId,
        String title,
        String description,
        String successUrl,
        String failureUrl,
        String pendingUrl
) {
}
