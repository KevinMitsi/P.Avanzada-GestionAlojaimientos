package com.avanzada.alojamientos.services.impl;

import com.avanzada.alojamientos.DTO.FavoriteDTO;
import com.avanzada.alojamientos.services.FavoriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class FavoriteServiceImpl implements FavoriteService {
    @Override
    public FavoriteDTO add(Long userId, Long accommodationId) {
        return null;
    }

    @Override
    public void remove(Long favoriteId) {
        log.info("Removing favorite with ID: {}", favoriteId);
    }

    @Override
    public List<FavoriteDTO> findByUser(Long userId) {
        return List.of();
    }
}
