package com.avanzada.alojamientos.DTO.auth;


import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;
import java.time.LocalDate;

public record RegisterUserDTO(
        @NotBlank @Length(max = 50) @Email String email,
        @NotBlank @Length(min = 8, max = 20)
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&._-])[A-Za-z\\d@$!%*?&._-]+$",
                message = "La contraseña debe contener al menos una mayúscula, un número y un símbolo (@$!%*?&)"
        ) String password,
        @NotBlank @Length(max = 100) String name,
        @NotBlank @Length(max = 10) String phone,
        @NotNull @Past LocalDate dateOfBirth

) {
}
