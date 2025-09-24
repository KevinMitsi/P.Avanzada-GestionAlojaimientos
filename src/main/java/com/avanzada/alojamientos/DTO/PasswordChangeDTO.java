package com.avanzada.alojamientos.DTO;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record PasswordChangeDTO(
        @NotBlank String currentPassword,
        @NotBlank @Length(min = 8,max = 15) String newPassword



) {
}
