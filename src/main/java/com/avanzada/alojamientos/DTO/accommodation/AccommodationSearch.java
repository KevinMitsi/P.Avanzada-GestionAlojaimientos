package com.avanzada.alojamientos.DTO.accommodation;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record AccommodationSearch(
        Long cityId,
        @Future LocalDate startDate,
        @Future LocalDate endDate,
        @Min(1) Integer guests,
        @Positive BigDecimal minPrice,
        @Positive BigDecimal maxPrice,

        List<String> services
) {}