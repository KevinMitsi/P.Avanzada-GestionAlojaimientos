package com.avanzada.alojamientos.DTO;

import com.avanzada.alojamientos.Model.NotificationType;

public record NotificationDTO(
        String id,
        String userId,
        String title,
        String body,
        NotificationType type,
        Object metadata,
        Boolean read,
        String createdAt
) {
}
