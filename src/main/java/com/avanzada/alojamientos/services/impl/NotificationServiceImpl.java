package com.avanzada.alojamientos.services.impl;

import com.avanzada.alojamientos.DTO.notification.CreateNotificationDTO;
import com.avanzada.alojamientos.DTO.notification.EmailDTO;
import com.avanzada.alojamientos.DTO.notification.NotificationDTO;
import com.avanzada.alojamientos.DTO.model.NotificationType;
import com.avanzada.alojamientos.entities.NotificationEntity;
import com.avanzada.alojamientos.entities.UserEntity;
import com.avanzada.alojamientos.exceptions.UserNotFoundException;
import com.avanzada.alojamientos.exceptions.NotificationNotFoundException;
import com.avanzada.alojamientos.mappers.NotificationMapper;
import com.avanzada.alojamientos.repositories.NotificationRepository;
import com.avanzada.alojamientos.repositories.UserRepository;
import com.avanzada.alojamientos.services.EmailNotificationService;

import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements EmailNotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    @Value("${SMTP_HOST:smtp.gmail.com}")
    private String smtpHost;

    @Value("${SMTP_PORT:587}")
    private int smtpPort;

    @Value("${SMTP_USERNAME:kegarrapala.2003@gmail.com}")
    private String smtpUsername;

    @Value("${SMTP_PASSWORD:zoih yqyq fjja tosn}")
    private String smtpPassword;

    @Override
    public NotificationDTO create(CreateNotificationDTO createNotificationDTO) {
        log.info("Creating notification using DTO for user: {} with title: {}",
                createNotificationDTO.userId(), createNotificationDTO.title());

        return create(
                createNotificationDTO.userId(),
                createNotificationDTO.title(),
                createNotificationDTO.body(),
                createNotificationDTO.metadata()
        );
    }

    @Override
    public NotificationDTO create(Long userId, String title, String body, Object metadata) {
        log.info("Creating notification for user: {} with title: {}", userId, title);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + userId));

        NotificationEntity notification = new NotificationEntity();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setBody(body);
        notification.setType(determineNotificationType(metadata));
        notification.setMetadata(metadata != null ? metadata.toString() : null);
        notification.setRead(false);

        NotificationEntity savedNotification = notificationRepository.save(notification);
        log.info("Notification created successfully with ID: {}", savedNotification.getId());

        // Usar el mapper para convertir la entidad a DTO
        return notificationMapper.toDTO(savedNotification);
    }

    @Override
    public Optional<NotificationDTO> findById(Long id) {
        log.debug("Finding notification by ID: {}", id);

        // Usar el mapper para convertir la entidad a DTO
        return notificationRepository.findById(id)
                .map(notificationMapper::toDTO);
    }

    @Override
    public List<NotificationDTO> findByUser(Long userId) {
        log.debug("Finding notifications for user: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("Usuario no encontrado con ID: " + userId);
        }

        // Usar el mapper para convertir la lista de entidades a DTOs
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(notificationMapper::toDTO)
                .toList();
    }

    @Override
    public void markAsRead(Long notificationId) {
        log.info("Marking notification as read: {}", notificationId);

        NotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException("Notificación no encontrada con ID: " + notificationId));

        notification.setRead(true);
        notificationRepository.save(notification);

        log.info("Notification {} marked as read successfully", notificationId);
    }

    @Override
    @Async
    public void sendMail(EmailDTO emailDTO) throws Exception {
        log.info("Sending email to: {} with subject: {}", emailDTO.recipient(), emailDTO.subject());

        Email email = EmailBuilder.startingBlank()
                .from(smtpUsername)
                .to(emailDTO.recipient())
                .withSubject(emailDTO.subject())
                .withPlainText(emailDTO.body())
                .buildEmail();

        try (Mailer mailer = MailerBuilder
                .withSMTPServer(smtpHost, smtpPort, smtpUsername, smtpPassword)
                .withTransportStrategy(TransportStrategy.SMTP_TLS)
                .withDebugLogging(true)
                .buildMailer()) {

            mailer.sendMail(email);
            log.info("Email sent successfully to: {}", emailDTO.recipient());
        } catch (Exception e) {
            log.error("Error sending email to: {}", emailDTO.recipient(), e);
            throw e;
        }
    }

    /**
     * Determina el tipo de notificación basado en los metadatos
     */
    private NotificationType determineNotificationType(Object metadata) {
        if (metadata == null) {
            return NotificationType.GENERAL;
        }

        String metadataStr = metadata.toString().toLowerCase();
        if (metadataStr.contains("reservation") || metadataStr.contains("reserva")) {
            if (metadataStr.contains("cancel")) {
                return NotificationType.CANCELLED_RESERVATION;
            }
            return NotificationType.NEW_RESERVATION;
        }

        if (metadataStr.contains("comment") || metadataStr.contains("comentario")) {
            return NotificationType.NEW_COMMENT;
        }

        return NotificationType.GENERAL;
    }
}
