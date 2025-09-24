package com.avanzada.alojamientos.DTO;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateReservationDTO(
        @NotBlank String accommodationId,
        @NotNull @Future LocalDate startDate,
        @NotNull @Future LocalDate endDate,
        @NotNull @Min(1) Integer guests
){
}
