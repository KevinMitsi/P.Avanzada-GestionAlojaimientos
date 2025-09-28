package com.avanzada.alojamientos.services;

import com.avanzada.alojamientos.DTO.other.FavoriteDTO;

import java.util.List;

public interface FavoriteService {
    FavoriteDTO add(Long userId, Long accommodationId);
    void remove(Long favoriteId);
    List<FavoriteDTO> findByUser(Long userId);
}
