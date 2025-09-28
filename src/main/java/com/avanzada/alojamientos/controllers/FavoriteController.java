package com.avanzada.alojamientos.controllers;

import com.avanzada.alojamientos.DTO.other.FavoriteDTO;
import com.avanzada.alojamientos.services.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{userId}/{accommodationId}")
    public FavoriteDTO add(@PathVariable Long userId, @PathVariable Long accommodationId) {
        return favoriteService.add(userId, accommodationId);
    }

    @DeleteMapping("/{favoriteId}")
    public void remove(@PathVariable Long favoriteId) {
        favoriteService.remove(favoriteId);
    }

    @GetMapping("/user/{userId}")
    public List<FavoriteDTO> findByUser(@PathVariable Long userId) {
        return favoriteService.findByUser(userId);
    }
}
