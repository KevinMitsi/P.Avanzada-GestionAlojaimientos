package com.avanzada.alojamientos.controllers;

import com.avanzada.alojamientos.DTO.other.FavoriteDTO;
import com.avanzada.alojamientos.security.CurrentUserService;
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
    private final CurrentUserService currentUserService;

    @PostMapping("/{accommodationId}")
    public ResponseEntity<FavoriteDTO> add(@PathVariable Long accommodationId) {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(favoriteService.add(userId, accommodationId));
    }

    @DeleteMapping("/{favoriteId}")
    public ResponseEntity<Void> remove(@PathVariable Long favoriteId) {
        favoriteService.remove(favoriteId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/me")
    public ResponseEntity<List<FavoriteDTO>> findByUser() {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(favoriteService.findByUser(userId));
    }
}
