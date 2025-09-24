package com.avanzada.alojamientos.DTO;

public record ResponseErrorDTO(
        Integer code,
        String message,
        Object details

) {
}
