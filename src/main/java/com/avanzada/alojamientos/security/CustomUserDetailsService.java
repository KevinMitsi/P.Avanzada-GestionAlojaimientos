package com.avanzada.alojamientos.security;

import com.avanzada.alojamientos.entities.UserEntity;
import com.avanzada.alojamientos.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        // Verificar que el usuario esté habilitado y no eliminado
        if (Boolean.TRUE.equals(user.getDeleted()) || !user.getEnabled()) {
            throw new UsernameNotFoundException("Usuario deshabilitado o eliminado: " + email);
        }

        // Crear authority basado en el rol del usuario
        String roleWithPrefix = "ROLE_" + user.getRole().name();
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(roleWithPrefix);

        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(Collections.singletonList(authority))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }

    /**
     * Obtiene la entidad de usuario completa por email
     */
    public UserEntity getUserEntityByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));
    }
}
