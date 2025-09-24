package com.avanzada.alojamientos.DTO;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record CityDTO(

        Integer id,
        String name,
        String country


) {
}
