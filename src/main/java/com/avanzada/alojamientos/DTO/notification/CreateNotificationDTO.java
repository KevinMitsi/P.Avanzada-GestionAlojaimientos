package com.avanzada.alojamientos.DTO.notification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateNotificationDTO(
        @NotNull(message = "El ID del usuario es obligatorio")
        Long userId,

        @NotBlank(message = "El título es obligatorio")
        String title,

        @NotBlank(message = "El cuerpo del mensaje es obligatorio")
        String body,

        String metadata
) {
}
