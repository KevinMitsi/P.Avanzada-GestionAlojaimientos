package com.avanzada.alojamientos.controllers;

import com.avanzada.alojamientos.DTO.other.FavoriteDTO;
import com.avanzada.alojamientos.services.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{userId}/{accommodationId}")
    public ResponseEntity<FavoriteDTO> add(@PathVariable Long userId, @PathVariable Long accommodationId) {
        return ResponseEntity.ok(favoriteService.add(userId, accommodationId));
    }

    @DeleteMapping("/{favoriteId}")
    public ResponseEntity<Void> remove(@PathVariable Long favoriteId) {
        favoriteService.remove(favoriteId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FavoriteDTO>> findByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(favoriteService.findByUser(userId));
    }
}
