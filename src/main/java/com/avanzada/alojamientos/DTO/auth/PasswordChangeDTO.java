package com.avanzada.alojamientos.DTO.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

public record PasswordChangeDTO(
        @NotBlank String currentPassword,
        @NotBlank @Length(min = 8, max = 20)
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
                message = "La contraseña debe contener al menos una mayúscula, un número y un símbolo (@$!%*?&)"
        ) String newPassword



) {
}
