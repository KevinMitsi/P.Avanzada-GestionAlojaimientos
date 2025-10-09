package com.avanzada.alojamientos.controllers;

import com.avanzada.alojamientos.DTO.model.NotificationType;
import com.avanzada.alojamientos.DTO.notification.CreateNotificationDTO;
import com.avanzada.alojamientos.DTO.notification.EmailDTO;
import com.avanzada.alojamientos.DTO.notification.NotificationDTO;
import com.avanzada.alojamientos.controllers.error.ControllerAdvice;
import com.avanzada.alojamientos.exceptions.NotificationNotFoundException;
import com.avanzada.alojamientos.security.CurrentUserService;
import com.avanzada.alojamientos.security.CustomUserDetailsService;
import com.avanzada.alojamientos.security.JwtAuthenticationFilter;
import com.avanzada.alojamientos.services.EmailNotificationService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ControllerAdvice.class, NotificationControllerTest.TestConfig.class})
class NotificationControllerTest {

    @Resource
    private MockMvc mockMvc;

    @Resource
    private EmailNotificationService notificationService;

    @Resource
    private CurrentUserService currentUserService;

    @BeforeEach
    void setUp() {
        reset(notificationService, currentUserService);
    }

    @Test
    void post_create_withValidBody_shouldReturn201_andNotification() throws Exception {
        // Arrange
        String validJson = """
            {
              "userId": 100,
              "title": "Nueva reserva confirmada",
              "body": "Tu reserva ha sido confirmada exitosamente",
              "metadata": "{\\"reservationId\\": 5}"
            }
            """;

        NotificationDTO response = new NotificationDTO(
                1L,
                100L,
                "Nueva reserva confirmada",
                "Tu reserva ha sido confirmada exitosamente",
                NotificationType.NEW_RESERVATION,
                "{\"reservationId\": 5}",
                false,
                "2025-01-01T00:00:00Z"
        );

        when(notificationService.create(any(CreateNotificationDTO.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(100))
                .andExpect(jsonPath("$.title").value("Nueva reserva confirmada"))
                .andExpect(jsonPath("$.body").value("Tu reserva ha sido confirmada exitosamente"))
                .andExpect(jsonPath("$.read").value(false));

        verify(notificationService, times(1)).create(any(CreateNotificationDTO.class));
    }

    @Test
    void post_create_withInvalidBody_shouldReturn400() throws Exception {
        // Arrange: userId null, título vacío, body vacío
        String invalidJson = """
            {
              "userId": null,
              "title": "",
              "body": "",
              "metadata": null
            }
            """;

        // Act & Assert
        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Error de validación")));

        verify(notificationService, never()).create(any(CreateNotificationDTO.class));
    }

    @Test
    void post_create_withMissingUserId_shouldReturn400() throws Exception {
        // Arrange
        String invalidJson = """
            {
              "title": "Título",
              "body": "Cuerpo del mensaje"
            }
            """;

        // Act & Assert
        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Error de validación")));

        verify(notificationService, never()).create(any(CreateNotificationDTO.class));
    }

    @Test
    void get_findById_whenFound_shouldReturn200_andNotification() throws Exception {
        // Arrange
        NotificationDTO notification = new NotificationDTO(
                5L,
                100L,
                "Notificación de prueba",
                "Cuerpo de la notificación",
                NotificationType.GENERAL,
                null,
                true,
                "2025-01-01T00:00:00Z"
        );

        when(notificationService.findById(5L)).thenReturn(Optional.of(notification));

        // Act & Assert
        mockMvc.perform(get("/api/notifications/{id}", 5L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.title").value("Notificación de prueba"))
                .andExpect(jsonPath("$.read").value(true));

        verify(notificationService, times(1)).findById(5L);
    }

    @Test
    void get_findById_whenNotFound_shouldReturn404() throws Exception {
        // Arrange
        when(notificationService.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/notifications/{id}", 999L))
                .andExpect(status().isNotFound());

        verify(notificationService, times(1)).findById(999L);
    }

    @Test
    void get_findByCurrentUser_shouldReturn200_andListOfNotifications() throws Exception {
        // Arrange
        NotificationDTO notification1 = new NotificationDTO(
                1L, 100L, "Notificación 1", "Cuerpo 1", NotificationType.NEW_COMMENT, null, false, "2025-01-01T00:00:00Z"
        );
        NotificationDTO notification2 = new NotificationDTO(
                2L, 100L, "Notificación 2", "Cuerpo 2", NotificationType.CANCELLED_RESERVATION, null, true, "2025-01-02T00:00:00Z"
        );

        when(currentUserService.getCurrentUserId()).thenReturn(100L);
        when(notificationService.findByUser(100L)).thenReturn(List.of(notification1, notification2));

        // Act & Assert
        mockMvc.perform(get("/api/notifications/user/me"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Notificación 1"))
                .andExpect(jsonPath("$[0].read").value(false))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].title").value("Notificación 2"))
                .andExpect(jsonPath("$[1].read").value(true));

        verify(currentUserService, times(1)).getCurrentUserId();
        verify(notificationService, times(1)).findByUser(100L);
    }

    @Test
    void get_findByCurrentUser_whenNoNotifications_shouldReturn200_andEmptyList() throws Exception {
        // Arrange
        when(currentUserService.getCurrentUserId()).thenReturn(100L);
        when(notificationService.findByUser(100L)).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/notifications/user/me"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(currentUserService, times(1)).getCurrentUserId();
        verify(notificationService, times(1)).findByUser(100L);
    }

    @Test
    void put_markAsRead_withValidId_shouldReturn200() throws Exception {
        // Arrange
        doNothing().when(notificationService).markAsRead(1L);

        // Act & Assert
        mockMvc.perform(put("/api/notifications/{id}/read", 1L))
                .andExpect(status().isOk());

        verify(notificationService, times(1)).markAsRead(1L);
    }

    @Test
    void put_markAsRead_whenNotificationNotFound_shouldReturn404() throws Exception {
        // Arrange
        doThrow(new NotificationNotFoundException("Notificación no encontrada"))
                .when(notificationService).markAsRead(anyLong());

        // Act & Assert
        mockMvc.perform(put("/api/notifications/{id}/read", 999L))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Notificación no encontrada")));
    }

    @Test
    void post_sendEmail_withValidBody_shouldReturn200() throws Exception {
        // Arrange
        String validJson = """
            {
              "subject": "Confirmación de reserva",
              "body": "Tu reserva ha sido confirmada",
              "recipient": "user@example.com"
            }
            """;

        doNothing().when(notificationService).sendMail(any(EmailDTO.class));

        // Act & Assert
        mockMvc.perform(post("/api/notifications/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Email enviado exitosamente")));

        verify(notificationService, times(1)).sendMail(any(EmailDTO.class));
    }

    @Test
    void post_sendEmail_whenServiceThrowsException_shouldReturn500() throws Exception {
        // Arrange
        String validJson = """
            {
              "subject": "Test",
              "body": "Test body",
              "recipient": "test@example.com"
            }
            """;

        doThrow(new RuntimeException("Error al enviar email"))
                .when(notificationService).sendMail(any(EmailDTO.class));

        // Act & Assert
        mockMvc.perform(post("/api/notifications/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Error interno del servidor")));
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        EmailNotificationService notificationService() {
            return mock(EmailNotificationService.class);
        }

        @Bean
        @Primary
        CurrentUserService currentUserService() {
            return mock(CurrentUserService.class);
        }

        @Bean
        @Primary
        JwtAuthenticationFilter jwtAuthenticationFilter() {
            return mock(JwtAuthenticationFilter.class);
        }

        @Bean
        @Primary
        CustomUserDetailsService customUserDetailsService() {
            return mock(CustomUserDetailsService.class);
        }
    }
}