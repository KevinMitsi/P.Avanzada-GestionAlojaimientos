package com.avanzada.alojamientos.repositories;

import com.avanzada.alojamientos.entities.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
}
