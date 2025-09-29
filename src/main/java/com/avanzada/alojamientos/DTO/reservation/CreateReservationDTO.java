package com.avanzada.alojamientos.DTO.reservation;

import jakarta.validation.constraints.Future;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateReservationDTO(
        @NotBlank Long accommodationId,
        @NotNull @Future LocalDate startDate,
        @NotNull @Future LocalDate endDate,
        @NotNull Long user
){
}
