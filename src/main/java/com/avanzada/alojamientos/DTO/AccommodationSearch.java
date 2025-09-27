package com.avanzada.alojamientos.DTO;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record AccommodationSearch(
        @Length(max = 100) String cityId,
        @Future LocalDate startDate,
        @Future LocalDate endDate,
        @Min(1) Integer guests,
        @Positive BigDecimal minPrice,
        @Positive BigDecimal maxPrice,

        List<String> services
) {}