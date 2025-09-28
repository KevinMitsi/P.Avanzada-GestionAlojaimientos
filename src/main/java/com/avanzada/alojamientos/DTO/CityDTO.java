package com.avanzada.alojamientos.DTO;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record CityDTO(

        long id,
        String name,
        String country


) {
}
