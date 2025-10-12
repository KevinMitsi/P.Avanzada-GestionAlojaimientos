package com.avanzada.alojamientos.controllers;

import com.avanzada.alojamientos.DTO.notification.CreateNotificationDTO;
import com.avanzada.alojamientos.DTO.notification.EmailDTO;
import com.avanzada.alojamientos.DTO.notification.NotificationDTO;
import com.avanzada.alojamientos.security.CurrentUserService;
import com.avanzada.alojamientos.services.EmailNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notificaciones", description = "API para gestión de notificaciones")
public class NotificationController {

    private final EmailNotificationService notificationService;
    private final CurrentUserService currentUserService;

    @PostMapping
    @Operation(summary = "Crear una nueva notificación")
    public ResponseEntity<NotificationDTO> createNotification(
            @Valid @RequestBody CreateNotificationDTO createNotificationDTO) {

        NotificationDTO notification = notificationService.create(createNotificationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(notification);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener notificación por ID")
    public ResponseEntity<NotificationDTO> getNotificationById(@PathVariable Long id) {
        Optional<NotificationDTO> notification = notificationService.findById(id);
        return notification
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/me")
    @Operation(summary = "Obtener notificaciones del usuario actual")
    public ResponseEntity<List<NotificationDTO>> getNotificationsByCurrentUser() {
        Long userId = currentUserService.getCurrentUserId();
        List<NotificationDTO> notifications = notificationService.findByUser(userId);
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Marcar notificación como leída")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        Long userId = currentUserService.getCurrentUserId();
        notificationService.markAsRead(userId, id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/email")
    @Operation(summary = "Enviar email")
    public ResponseEntity<String> sendEmail(@RequestBody EmailDTO emailDTO) throws Exception {
        notificationService.sendMail(emailDTO);
        return ResponseEntity.ok("Email enviado exitosamente");
    }
}
