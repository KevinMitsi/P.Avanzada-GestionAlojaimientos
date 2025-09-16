package com.avanzada.alojamientos.DTO;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record PasswordResetDto(
    @NotBlank String token,
    @NotBlank @Length(min = 8, max = 20) String newPassword)
{
}
