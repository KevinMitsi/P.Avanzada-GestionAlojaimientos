package com.avanzada.alojamientos.repositories;

import com.avanzada.alojamientos.entities.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    /**
     * Encuentra todas las notificaciones de un usuario ordenadas por fecha de creación descendente
     */
    List<NotificationEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Encuentra todas las notificaciones no leídas de un usuario
     */
    List<NotificationEntity> findByUserIdAndReadFalseOrderByCreatedAtDesc(Long userId);

    /**
     * Cuenta las notificaciones no leídas de un usuario
     */
    long countByUserIdAndReadFalse(Long userId);
}
