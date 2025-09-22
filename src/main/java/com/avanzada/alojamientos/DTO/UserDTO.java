package com.avanzada.alojamientos.DTO;

import com.avanzada.alojamientos.DTO.model.Role;

import java.time.LocalDate;
import java.util.List;

public record UserDTO(
        String id,
        String name,
        String email,
        String phone,
        LocalDate dateOfBirth,
        Role role,
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