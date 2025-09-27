package com.avanzada.alojamientos.DTO;


import com.avanzada.alojamientos.DTO.model.NotificationType;

public record NotificationDTO(
        String id,
        String userId,
        String title,
        String body,
        NotificationType type,
        String metadata,
        Boolean read,
        String createdAt
) {
}
