package com.avanzada.alojamientos.DTO.user;

import com.avanzada.alojamientos.DTO.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import org.hibernate.validator.constraints.Length;
import java.time.LocalDate;

public record CreateUserDTO(
        @NotBlank @Length(max = 100) String name,
        @Length(max = 10) String phone,
        @NotBlank @Length(max = 50) @Email String email,
        @NotBlank @Length(min = 8, max = 20) String password,
        @NotNull @Past LocalDate dateBirth,
        @NotNull UserRole role
) {
}