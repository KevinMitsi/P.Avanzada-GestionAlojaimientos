package com.avanzada.alojamientos.DTO.other;

import jakarta.validation.constraints.NotNull;

public record CoordinatesDTO(
        @NotNull Double lat,

        @NotNull Double lng
) {
}
