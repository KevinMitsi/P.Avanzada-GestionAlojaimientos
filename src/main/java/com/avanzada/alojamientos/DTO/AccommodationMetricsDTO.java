package com.avanzada.alojamientos.DTO;

import java.math.BigDecimal;

public record AccommodationMetricsDTO(
        Long totalReservations,
        Double averageRating,
        BigDecimal totalRevenue
) {}