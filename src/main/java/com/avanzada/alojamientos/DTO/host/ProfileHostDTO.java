package com.avanzada.alojamientos.DTO.host;

import java.time.LocalDateTime;

public record ProfileHostDTO(
        long id,
        long hostId,
        String businessName,
        Boolean verified,
        LocalDateTime createdAt

) {
}
