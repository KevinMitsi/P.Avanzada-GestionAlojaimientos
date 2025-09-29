package com.avanzada.alojamientos.DTO.host;

import java.time.LocalDateTime;

public record ProfileHostDTO(
        Long id,
        Long hostId,
        String businessName,
        Boolean verified,
        LocalDateTime createdAt

) {
}
