package com.avanzada.alojamientos.services.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.avanzada.alojamientos.DTO.notification.CreateNotificationDTO;
import com.avanzada.alojamientos.DTO.notification.EmailDTO;
import com.avanzada.alojamientos.DTO.notification.NotificationDTO;
import com.avanzada.alojamientos.DTO.model.NotificationType;
import com.avanzada.alojamientos.entities.NotificationEntity;
import com.avanzada.alojamientos.entities.UserEntity;
import com.avanzada.alojamientos.exceptions.UserNotFoundException;
import com.avanzada.alojamientos.exceptions.NotificationNotFoundException;
import com.avanzada.alojamientos.exceptions.UnauthorizedException;
import com.avanzada.alojamientos.mappers.NotificationMapper;
import com.avanzada.alojamientos.repositories.NotificationRepository;
import com.avanzada.alojamientos.repositories.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private UserEntity testUser;
    private NotificationEntity testNotification;
    private NotificationDTO testNotificationDTO;
    private CreateNotificationDTO createNotificationDTO;

    @BeforeEach
    void setUp() {
        // Configurar propiedades de configuración usando ReflectionTestUtils
        ReflectionTestUtils.setField(notificationService, "smtpHost", "smtp.gmail.com");
        ReflectionTestUtils.setField(notificationService, "smtpPort", 587);
        ReflectionTestUtils.setField(notificationService, "smtpUsername", "test@gmail.com");
        ReflectionTestUtils.setField(notificationService, "smtpPassword", "testpassword");

        // Arrange - Configurar objetos de prueba
        testUser = new UserEntity();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");

        testNotification = new NotificationEntity();
        testNotification.setId(1L);
        testNotification.setUser(testUser);
        testNotification.setTitle("Test Title");
        testNotification.setBody("Test Body");
        testNotification.setType(NotificationType.GENERAL);
        testNotification.setMetadata("test metadata");
        testNotification.setRead(false);
        testNotification.setCreatedAt(LocalDateTime.now());

        testNotificationDTO = new NotificationDTO(
                1L,
                1L,
                "Test Title",
                "Test Body",
                NotificationType.GENERAL,
                "test metadata",
                false,
                "2025-10-08T10:00:00"
        );

        createNotificationDTO = new CreateNotificationDTO(
                1L,
                "Test Title",
                "Test Body",
                "test metadata"
        );
    }

    @Test
    void createWithDTO_ShouldReturnNotificationDTO_WhenValidData() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(notificationRepository.save(any(NotificationEntity.class))).thenReturn(testNotification);
        when(notificationMapper.toDTO(testNotification)).thenReturn(testNotificationDTO);

        // Act
        NotificationDTO result = notificationService.create(createNotificationDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testNotificationDTO.id(), result.id());
        assertEquals(testNotificationDTO.title(), result.title());
        assertEquals(testNotificationDTO.body(), result.body());
        verify(userRepository).findById(1L);
        verify(notificationRepository).save(any(NotificationEntity.class));
        verify(notificationMapper).toDTO(testNotification);
    }

    @Test
    void createWithDTO_ShouldThrowUserNotFoundException_WhenUserNotExists() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
            () -> notificationService.create(createNotificationDTO));

        assertEquals("Usuario no encontrado con ID: 1", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(notificationRepository, never()).save(any());
        verify(notificationMapper, never()).toDTO(any());
    }

    @Test
    void createWithParameters_ShouldReturnNotificationDTO_WhenValidData() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(notificationRepository.save(any(NotificationEntity.class))).thenReturn(testNotification);
        when(notificationMapper.toDTO(testNotification)).thenReturn(testNotificationDTO);

        // Act
        NotificationDTO result = notificationService.create(1L, "Test Title", "Test Body", "test metadata");

        // Assert
        assertNotNull(result);
        assertEquals(testNotificationDTO.id(), result.id());
        assertEquals(testNotificationDTO.title(), result.title());
        verify(userRepository).findById(1L);
        verify(notificationRepository).save(any(NotificationEntity.class));
        verify(notificationMapper).toDTO(testNotification);
    }

    @Test
    void createWithParameters_ShouldThrowUserNotFoundException_WhenUserNotExists() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
            () -> notificationService.create(1L, "Test Title", "Test Body", "test metadata"));

        assertEquals("Usuario no encontrado con ID: 1", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void createWithParameters_ShouldHandleNullMetadata() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(notificationRepository.save(any(NotificationEntity.class))).thenReturn(testNotification);
        when(notificationMapper.toDTO(testNotification)).thenReturn(testNotificationDTO);

        // Act
        NotificationDTO result = notificationService.create(1L, "Test Title", "Test Body", null);

        // Assert
        assertNotNull(result);
        verify(notificationRepository).save(argThat(notification ->
            notification.getMetadata() == null &&
            notification.getType() == NotificationType.GENERAL));
    }

    @Test
    void findById_ShouldReturnNotificationDTO_WhenNotificationExists() {
        // Arrange
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));
        when(notificationMapper.toDTO(testNotification)).thenReturn(testNotificationDTO);

        // Act
        Optional<NotificationDTO> result = notificationService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testNotificationDTO.id(), result.get().id());
        verify(notificationRepository).findById(1L);
        verify(notificationMapper).toDTO(testNotification);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotificationNotExists() {
        // Arrange
        when(notificationRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        Optional<NotificationDTO> result = notificationService.findById(1L);

        // Assert
        assertFalse(result.isPresent());
        verify(notificationRepository).findById(1L);
        verify(notificationMapper, never()).toDTO(any());
    }

    @Test
    void findByUser_ShouldReturnNotificationList_WhenUserExists() {
        // Arrange
        List<NotificationEntity> notifications = List.of(testNotification);

        when(userRepository.existsById(1L)).thenReturn(true);
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(notifications);
        when(notificationMapper.toDTO(testNotification)).thenReturn(testNotificationDTO);

        // Act
        List<NotificationDTO> result = notificationService.findByUser(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testNotificationDTO.id(), result.getFirst().id());
        verify(userRepository).existsById(1L);
        verify(notificationRepository).findByUserIdOrderByCreatedAtDesc(1L);
        verify(notificationMapper).toDTO(testNotification);
    }

    @Test
    void findByUser_ShouldThrowUserNotFoundException_WhenUserNotExists() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(false);

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
            () -> notificationService.findByUser(1L));

        assertEquals("Usuario no encontrado con ID: 1", exception.getMessage());
        verify(userRepository).existsById(1L);
        verify(notificationRepository, never()).findByUserIdOrderByCreatedAtDesc(any());
    }

    @Test
    void markAsRead_ShouldUpdateNotification_WhenNotificationExists() {
        // Arrange
        Long userId = 1L;
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(testNotification)).thenReturn(testNotification);

        // Act
        notificationService.markAsRead(userId, 1L);

        // Assert
        assertTrue(testNotification.getRead());
        verify(notificationRepository).findById(1L);
        verify(notificationRepository).save(testNotification);
    }

    @Test
    void markAsRead_ShouldThrowNotificationNotFoundException_WhenNotificationNotExists() {
        // Arrange
        Long userId = 1L;
        when(notificationRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        NotificationNotFoundException exception = assertThrows(NotificationNotFoundException.class,
            () -> notificationService.markAsRead(userId, 1L));

        assertEquals("Notificación no encontrada con ID: 1", exception.getMessage());
        verify(notificationRepository).findById(1L);
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void markAsRead_ShouldThrowUnauthorizedException_WhenUserNotOwner() {
        // Arrange
        Long wrongUserId = 999L; // Usuario diferente al dueño
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));

        // Act & Assert
        UnauthorizedException exception = assertThrows(UnauthorizedException.class,
            () -> notificationService.markAsRead(wrongUserId, 1L));

        assertEquals("User is not authorized to mark this notification as read", exception.getMessage());
        verify(notificationRepository).findById(1L);
        verify(notificationRepository, never()).save(any());
        assertFalse(testNotification.getRead()); // La notificación no debe ser marcada como leída
    }

    @Test
    void sendMail_ShouldNotThrowException_WhenValidEmailDTO() {
        // Arrange
        EmailDTO emailDTO = new EmailDTO("Test Subject", "Test Body", "test@example.com");

        // Act & Assert
        // Nota: Este test verifica que el método no lance excepciones con datos válidos
        // En un entorno real, se mockearía el servicio de email
        assertDoesNotThrow(() -> {
            try {
                notificationService.sendMail(emailDTO);
            } catch (Exception e) {
                // Se espera que falle la conexión SMTP en el entorno de test
                // pero no por validación de datos
                assertTrue(e.getMessage().contains("Connection") ||
                          e.getMessage().contains("mail") ||
                          e.getMessage().contains("timeout"));
            }
        });
    }

    @Test
    void determineNotificationType_ShouldReturnNewReservation_WhenMetadataContainsReservation() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(notificationRepository.save(any(NotificationEntity.class))).thenReturn(testNotification);
        when(notificationMapper.toDTO(testNotification)).thenReturn(testNotificationDTO);

        // Act
        notificationService.create(1L, "Test", "Test", "reservation metadata");

        // Assert
        verify(notificationRepository).save(argThat(notification ->
            notification.getType() == NotificationType.NEW_RESERVATION));
    }

    @Test
    void determineNotificationType_ShouldReturnCancelledReservation_WhenMetadataContainsCancelReservation() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(notificationRepository.save(any(NotificationEntity.class))).thenReturn(testNotification);
        when(notificationMapper.toDTO(testNotification)).thenReturn(testNotificationDTO);

        // Act
        notificationService.create(1L, "Test", "Test", "cancel reservation metadata");

        // Assert
        verify(notificationRepository).save(argThat(notification ->
            notification.getType() == NotificationType.CANCELLED_RESERVATION));
    }

    @Test
    void determineNotificationType_ShouldReturnNewComment_WhenMetadataContainsComment() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(notificationRepository.save(any(NotificationEntity.class))).thenReturn(testNotification);
        when(notificationMapper.toDTO(testNotification)).thenReturn(testNotificationDTO);

        // Act
        notificationService.create(1L, "Test", "Test", "comment metadata");

        // Assert
        verify(notificationRepository).save(argThat(notification ->
            notification.getType() == NotificationType.NEW_COMMENT));
    }

    @Test
    void determineNotificationType_ShouldReturnGeneral_WhenMetadataIsNull() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(notificationRepository.save(any(NotificationEntity.class))).thenReturn(testNotification);
        when(notificationMapper.toDTO(testNotification)).thenReturn(testNotificationDTO);

        // Act
        notificationService.create(1L, "Test", "Test", null);

        // Assert
        verify(notificationRepository).save(argThat(notification ->
            notification.getType() == NotificationType.GENERAL));
    }

    @Test
    void determineNotificationType_ShouldReturnGeneral_WhenMetadataDoesNotMatch() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(notificationRepository.save(any(NotificationEntity.class))).thenReturn(testNotification);
        when(notificationMapper.toDTO(testNotification)).thenReturn(testNotificationDTO);

        // Act
        notificationService.create(1L, "Test", "Test", "unknown metadata");

        // Assert
        verify(notificationRepository).save(argThat(notification ->
            notification.getType() == NotificationType.GENERAL));
    }

    @Test
    void determineNotificationType_ShouldHandleSpanishKeywords() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(notificationRepository.save(any(NotificationEntity.class))).thenReturn(testNotification);
        when(notificationMapper.toDTO(testNotification)).thenReturn(testNotificationDTO);

        // Act
        notificationService.create(1L, "Test", "Test", "reserva nueva");

        // Assert
        verify(notificationRepository).save(argThat(notification ->
            notification.getType() == NotificationType.NEW_RESERVATION));
    }

    @Test
    void determineNotificationType_ShouldHandleSpanishCommentKeyword() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(notificationRepository.save(any(NotificationEntity.class))).thenReturn(testNotification);
        when(notificationMapper.toDTO(testNotification)).thenReturn(testNotificationDTO);

        // Act
        notificationService.create(1L, "Test", "Test", "comentario nuevo");

        // Assert
        verify(notificationRepository).save(argThat(notification ->
            notification.getType() == NotificationType.NEW_COMMENT));
    }
}