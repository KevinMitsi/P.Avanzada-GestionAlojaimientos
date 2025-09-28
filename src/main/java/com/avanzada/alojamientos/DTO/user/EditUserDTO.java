package com.avanzada.alojamientos.DTO.user;

import com.avanzada.alojamientos.DTO.model.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import org.hibernate.validator.constraints.Length;
import java.time.LocalDate;

public record EditUserDTO(
        @NotBlank @Length(max = 100) String name,
        @Length(max = 10) String phone,
        @Length(max = 300) String photoUrl,
        @NotNull @Past LocalDate dateBirth,
        @NotNull UserRole role
) {
}
