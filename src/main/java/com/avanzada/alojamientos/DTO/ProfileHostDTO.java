package com.avanzada.alojamientos.DTO;

import java.time.LocalDateTime;

public record ProfileHostDTO(
        long id,
        long hostId,
        String businessName,
        Boolean verified,
        LocalDateTime createdAt

) {
}
