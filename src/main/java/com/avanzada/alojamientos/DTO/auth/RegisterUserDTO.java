package com.avanzada.alojamientos.DTO.auth;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import org.hibernate.validator.constraints.Length;
import java.time.LocalDate;

public record RegisterUserDTO(
        @NotBlank @Length(max = 50) @Email String email,
        @NotBlank @Length(min = 8, max = 20) String password,
        @NotBlank @Length(max = 100) String name,
        @NotBlank @Length(max = 10) String phone,
        @NotNull @Past LocalDate dateOfBirth

) {
}
