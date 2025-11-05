package com.avanzada.alojamientos.DTO.reservation;


import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateReservationDTO(
        @NotNull Long accommodationId,
        @NotNull @FutureOrPresent LocalDate startDate,
        @NotNull @FutureOrPresent LocalDate endDate,
        @NotNull @Min(1) Integer guests
){
}
