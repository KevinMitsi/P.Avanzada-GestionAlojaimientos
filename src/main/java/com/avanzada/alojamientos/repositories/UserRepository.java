package com.avanzada.alojamientos.repositories;

import com.avanzada.alojamientos.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    // Busca un usuario por su email

    Optional<UserEntity> findByEmail(String email);

    // Verifica si existe un usuario con el email dado

    boolean existsByEmail(String email);

    Optional<UserEntity> findById(Long id);
}
