package com.avanzada.alojamientos.DTO;

import java.time.LocalDateTime;

public record ReplyHostDTO(
        Integer hostId,
        String text,
        LocalDateTime createdAt

) {
}
