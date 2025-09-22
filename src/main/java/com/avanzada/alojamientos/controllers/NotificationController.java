package com.avanzada.alojamientos.controllers;

import com.avanzada.alojamientos.DTO.NotificationDTO;
import com.avanzada.alojamientos.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/{userId}")
    public NotificationDTO create(@PathVariable Long userId,
                                  @RequestParam String title,
                                  @RequestParam String body,
                                  @RequestBody Object metadata) {
        return notificationService.create(userId, title, body, metadata);
    }

    @GetMapping("/{id}")
    public Optional<NotificationDTO> findById(@PathVariable Long id) {
        return notificationService.findById(id);
    }

    @GetMapping("/user/{userId}")
    public List<NotificationDTO> findByUser(@PathVariable Long userId) {
        return notificationService.findByUser(userId);
    }

    @PutMapping("/{notificationId}/read")
    public void markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
    }
}

