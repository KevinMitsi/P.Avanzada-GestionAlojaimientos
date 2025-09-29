package com.avanzada.alojamientos.DTO.other;

import java.time.LocalDateTime;

public record ImageDTO(
        Long id,
        String url,
        String thumbnailUrl,
        Boolean isPrimary,
        LocalDateTime createdAt
) {
}
