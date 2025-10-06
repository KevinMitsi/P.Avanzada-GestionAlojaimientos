package com.avanzada.alojamientos.DTO.user;

import com.avanzada.alojamientos.DTO.model.UserRole;


import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public record UserDTO(
        Long id,
        String name,
        String email,
        String phone,
        LocalDate dateOfBirth,
        Set<UserRole> roles,
        String avatarUrl,
        String description,
        List<String> documentsUrl,
        Boolean verified,
        Boolean enabled,
        String createdAt,
        String updatedAt,
        Boolean deleted
) {
}