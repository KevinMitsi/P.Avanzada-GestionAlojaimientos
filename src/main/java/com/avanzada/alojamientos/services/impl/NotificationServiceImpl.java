package com.avanzada.alojamientos.services.impl;

import com.avanzada.alojamientos.DTO.notification.NotificationDTO;
import com.avanzada.alojamientos.services.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    @Override
    public NotificationDTO create(Long userId, String title, String body, Object metadata) {
        return null;
    }

    @Override
    public Optional<NotificationDTO> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public List<NotificationDTO> findByUser(Long userId) {
        return List.of();
    }

    @Override
    public void markAsRead(Long notificationId) {

    }
}
