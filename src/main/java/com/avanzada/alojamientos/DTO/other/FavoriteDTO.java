package com.avanzada.alojamientos.DTO.other;

public record FavoriteDTO(
        Long id,
        Long userId,
        FavoriteAccommodationDTO accommodation,
        String createdA
) {
}
