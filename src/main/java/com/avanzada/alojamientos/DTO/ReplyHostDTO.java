package com.avanzada.alojamientos.DTO;

import java.time.LocalDateTime;

public record ReplyHostDTO(
        long hostId,
        String text,
        LocalDateTime createdAt

) {
}
