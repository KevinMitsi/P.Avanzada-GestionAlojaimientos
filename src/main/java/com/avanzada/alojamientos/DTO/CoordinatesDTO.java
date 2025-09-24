package com.avanzada.alojamientos.DTO;

import jakarta.validation.constraints.NotNull;

public record CoordinatesDTO(
        @NotNull Double lat,

        @NotNull Double lng
) {
}
