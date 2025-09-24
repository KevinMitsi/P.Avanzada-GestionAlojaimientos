package com.avanzada.alojamientos.DTO;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record CreateCityDTO (
        @NotBlank @Length String name,
        @NotBlank @Length String country
){
}
