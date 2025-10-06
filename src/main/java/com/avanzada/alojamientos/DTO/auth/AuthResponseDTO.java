package com.avanzada.alojamientos.DTO.auth;

import com.avanzada.alojamientos.DTO.model.UserRole;

public record AuthResponseDTO(
        String token,
        String type,
        Long userId,
        String email,
        String name,
        UserRole role,
        boolean isVerified,
        boolean isHostVerified
) {
    public AuthResponseDTO(String token, Long userId, String email, String name, UserRole role, boolean isVerified, boolean isHostVerified) {
        this(token, "Bearer", userId, email, name, role, isVerified, isHostVerified);
    }
}
