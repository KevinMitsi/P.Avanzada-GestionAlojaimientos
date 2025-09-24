package com.avanzada.alojamientos.DTO;

import java.time.LocalDateTime;

public record ImageDTO(
        Integer id,
        String url,
        String thumbnailUrl,
        Boolean isPrimary,
        LocalDateTime createdAt
) {
}
