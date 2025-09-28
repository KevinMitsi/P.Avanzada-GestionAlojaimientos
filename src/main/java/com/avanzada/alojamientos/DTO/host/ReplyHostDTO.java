package com.avanzada.alojamientos.DTO.host;

import java.time.LocalDateTime;

public record ReplyHostDTO(
        long hostId,
        String text,
        LocalDateTime createdAt

) {
}
