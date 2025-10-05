package com.avanzada.alojamientos.repositories;

import com.avanzada.alojamientos.entities.PasswordResetTokenEntity;
import com.avanzada.alojamientos.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, Long> {

    // Busca un token por su hash que no haya sido usado

    Optional<PasswordResetTokenEntity> findByTokenHashAndUsedFalse(String tokenHash);

    // Busca todos los tokens no usados de un usuario

    List<PasswordResetTokenEntity> findByUserAndUsedFalse(UserEntity user);

}
