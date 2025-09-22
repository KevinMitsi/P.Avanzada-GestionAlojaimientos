package com.avanzada.alojamientos.DTO;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record DateRangeDTO(
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate
) {}