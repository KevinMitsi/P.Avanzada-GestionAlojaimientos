package com.avanzada.alojamientos.security;

import com.avanzada.alojamientos.entities.UserEntity;
import com.avanzada.alojamientos.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No hay usuario autenticado en el contexto");
        }
        return authentication.getName(); // username = email
    }

    public UserEntity getCurrentUser() {
        String email = getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuario actual no encontrado: " + email));
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public Long getCurrentHostId() {
        // En el modelo, el host es el mismo UserEntity (Accommodation.host es UserEntity)
        return getCurrentUserId();
    }

    public boolean isCurrentUserHost() {
        UserEntity user = getCurrentUser();
        return user.getHostProfile() != null; // o por rol si aplica
    }
}

