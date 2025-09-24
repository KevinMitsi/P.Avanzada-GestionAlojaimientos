package com.avanzada.alojamientos.DTO;

import java.time.LocalDateTime;

public record ProfileHostDTO(
        Integer id,
        Integer hostId,
        String businessName,
        Boolean verified,
        LocalDateTime createdAt

) {
}
