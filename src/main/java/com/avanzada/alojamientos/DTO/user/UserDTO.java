package com.avanzada.alojamientos.DTO.user;

import com.avanzada.alojamientos.DTO.model.UserRole;


import java.time.LocalDate;
import java.util.List;

public record UserDTO(
        long id,
        String name,
        String email,
        String phone,
        LocalDate dateOfBirth,
        UserRole role,
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