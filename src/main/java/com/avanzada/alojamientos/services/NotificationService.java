package com.avanzada.alojamientos.services;

import com.avanzada.alojamientos.DTO.notification.CreateNotificationDTO;
import com.avanzada.alojamientos.DTO.notification.NotificationDTO;

import java.util.List;
import java.util.Optional;

public interface NotificationService {
    NotificationDTO create(Long userId, String title, String body, Object metadata);
    NotificationDTO create(CreateNotificationDTO createNotificationDTO);
    Optional<NotificationDTO> findById(Long id);
    List<NotificationDTO> findByUser(Long userId);
    void markAsRead(Long userId, Long notificationId);
}
