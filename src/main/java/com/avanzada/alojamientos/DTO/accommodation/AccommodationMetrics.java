package com.avanzada.alojamientos.DTO.accommodation;

import java.math.BigDecimal;

public record AccommodationMetrics(
        Long totalReservations,
        Double averageRating,
        BigDecimal totalRevenue
) {}