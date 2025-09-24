package com.avanzada.alojamientos.services;

import com.avanzada.alojamientos.DTO.NotificationDTO;

import java.util.List;
import java.util.Optional;

public interface NotificationService {
    NotificationDTO create(Long userId, String title, String body, Object metadata);
    Optional<NotificationDTO> findById(Long id);
    List<NotificationDTO> findByUser(Long userId);
    void markAsRead(Long notificationId);
}
