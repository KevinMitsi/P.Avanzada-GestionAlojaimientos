package com.avanzada.alojamientos.repositories;

import com.avanzada.alojamientos.entities.PasswordResetTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, Long> {
}
