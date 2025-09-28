package com.avanzada.alojamientos.DTO.other;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record DateRange(
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate
) {}