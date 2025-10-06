package com.avanzada.alojamientos.DTO.other;

public record ResponseDTO<T>(
        boolean success,
        String message

) { }
