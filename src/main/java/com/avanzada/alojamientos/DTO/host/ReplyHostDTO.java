package com.avanzada.alojamientos.DTO.host;

import java.time.LocalDateTime;

public record ReplyHostDTO(
        Long hostId,
        String text,
        LocalDateTime createdAt
) {
}
