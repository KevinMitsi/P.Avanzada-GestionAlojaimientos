package com.avanzada.alojamientos.DTO.user;


import jakarta.validation.constraints.Past;
import org.hibernate.validator.constraints.Length;
import java.time.LocalDate;

public record EditUserDTO(
        @Length(max = 100) String name,
        @Length(max = 10) String phone,
        @Past LocalDate dateBirth,
        @Length(max = 500) String description
) {
}
