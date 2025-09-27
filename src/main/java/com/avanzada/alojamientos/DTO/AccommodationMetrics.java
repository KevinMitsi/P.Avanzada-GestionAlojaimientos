package com.avanzada.alojamientos.DTO;

import java.math.BigDecimal;

public record AccommodationMetrics(
        Long totalReservations,
        Double averageRating,
        BigDecimal totalRevenue
) {}