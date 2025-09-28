package com.avanzada.alojamientos.DTO.notification;

public record ResponseErrorDTO(
        Integer code,
        String message,
        Object details

) {
}
