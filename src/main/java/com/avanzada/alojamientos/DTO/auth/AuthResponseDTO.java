package com.avanzada.alojamientos.DTO.auth;

import com.avanzada.alojamientos.DTO.model.UserRole;

import java.util.Set;

public record AuthResponseDTO(
        String token,
        String type,
        Long userId,
        String email,
        String name,
        Set<UserRole> roles,
        boolean isVerified,
        boolean isHostVerified
) {
    public AuthResponseDTO(String token, Long userId, String email, String name, Set<UserRole> roles, boolean isVerified, boolean isHostVerified) {
        this(token, "Bearer", userId, email, name, roles, isVerified, isHostVerified);
    }
}
