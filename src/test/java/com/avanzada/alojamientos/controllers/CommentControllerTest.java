package com.avanzada.alojamientos.controllers;

import com.avanzada.alojamientos.DTO.comment.CommentDTO;
import com.avanzada.alojamientos.DTO.comment.CreateCommentDTO;
import com.avanzada.alojamientos.controllers.error.ControllerAdvice;
import com.avanzada.alojamientos.exceptions.CommentForbiddenException;
import com.avanzada.alojamientos.exceptions.CommentNotFoundException;
import com.avanzada.alojamientos.exceptions.ReservationNotFoundException;
import com.avanzada.alojamientos.security.CurrentUserService;
import com.avanzada.alojamientos.security.CustomUserDetailsService;
import com.avanzada.alojamientos.security.JwtAuthenticationFilter;
import com.avanzada.alojamientos.services.CommentService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CommentController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ControllerAdvice.class, CommentControllerTest.TestConfig.class})
class CommentControllerTest {

    @Resource
    private MockMvc mockMvc;

    @Resource
    private CommentService commentService;

    @Resource
    private CurrentUserService currentUserService;

    @BeforeEach
    void setUp() {
        reset(commentService, currentUserService);
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void post_create_withValidBody_shouldReturn200_andComment() throws Exception {
        // Arrange
        String validJson = """
            {
              "rating": 5,
              "text": "Excelente alojamiento, muy limpio y cómodo"
            }
            """;

        CommentDTO response = new CommentDTO(
                1L,
                5.0f,
                "Excelente alojamiento, muy limpio y cómodo",
                LocalDateTime.now(),
                false,
                1L,
                10L,
                100L,
                null
        );

        when(currentUserService.getCurrentUserId()).thenReturn(100L);
        when(commentService.create(eq(100L), eq(1L), any(CreateCommentDTO.class), eq(10L)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/comments/{reservationId}/{accommodationId}", 1L, 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rating").value(5.0))
                .andExpect(jsonPath("$.text").value("Excelente alojamiento, muy limpio y cómodo"))
                .andExpect(jsonPath("$.userId").value(100));

        verify(currentUserService, times(1)).getCurrentUserId();
        verify(commentService, times(1)).create(eq(100L), eq(1L), any(CreateCommentDTO.class), eq(10L));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void post_create_withInvalidBody_shouldReturn400() throws Exception {
        // Arrange: rating fuera de rango y texto vacío
        String invalidJson = """
            {
              "rating": 6,
              "text": ""
            }
            """;

        // Act & Assert
        mockMvc.perform(post("/api/comments/{reservationId}/{accommodationId}", 1L, 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Error de validación")));

        verify(commentService, never()).create(anyLong(), anyLong(), any(CreateCommentDTO.class), anyLong());
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void post_create_withTextTooLong_shouldReturn400() throws Exception {
        // Arrange: texto muy largo (más de 500 caracteres)
        String longText = "a".repeat(501);
        String invalidJson = String.format("""
            {
              "rating": 5,
              "text": "%s"
            }
            """, longText);

        // Act & Assert
        mockMvc.perform(post("/api/comments/{reservationId}/{accommodationId}", 1L, 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Error de validación")));

        verify(commentService, never()).create(anyLong(), anyLong(), any(CreateCommentDTO.class), anyLong());
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void post_create_whenReservationNotFound_shouldReturn404() throws Exception {
        // Arrange
        String validJson = """
            {
              "rating": 5,
              "text": "Excelente alojamiento"
            }
            """;

        when(currentUserService.getCurrentUserId()).thenReturn(100L);
        when(commentService.create(anyLong(), anyLong(), any(CreateCommentDTO.class), anyLong()))
                .thenThrow(new ReservationNotFoundException("Reserva no encontrada"));

        // Act & Assert
        mockMvc.perform(post("/api/comments/{reservationId}/{accommodationId}", 999L, 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Reserva no encontrada")));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void post_create_whenUserNotAuthorized_shouldReturn403() throws Exception {
        // Arrange
        String validJson = """
            {
              "rating": 5,
              "text": "Excelente alojamiento"
            }
            """;

        when(currentUserService.getCurrentUserId()).thenReturn(100L);
        when(commentService.create(anyLong(), anyLong(), any(CreateCommentDTO.class), anyLong()))
                .thenThrow(new CommentForbiddenException("No tienes permiso para comentar esta reserva"));

        // Act & Assert
        mockMvc.perform(post("/api/comments/{reservationId}/{accommodationId}", 1L, 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("Operación prohibida en comentarios")));
    }

    @Test
    void get_findById_shouldReturn200_andComment() throws Exception {
        // Arrange
        CommentDTO comment = new CommentDTO(
                5L,
                4.5f,
                "Muy buen lugar",
                LocalDateTime.now(),
                true,
                1L,
                10L,
                100L,
                null
        );

        when(commentService.findById(5L)).thenReturn(comment);

        // Act & Assert
        mockMvc.perform(get("/api/comments/{commentId}", 5L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.rating").value(4.5))
                .andExpect(jsonPath("$.text").value("Muy buen lugar"));

        verify(commentService, times(1)).findById(5L);
    }

    @Test
    void get_findById_whenCommentNotFound_shouldReturn404() throws Exception {
        // Arrange
        when(commentService.findById(999L))
                .thenThrow(new CommentNotFoundException("Comentario no encontrado"));

        // Act & Assert
        mockMvc.perform(get("/api/comments/{commentId}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Comentario no encontrado")));
    }

    @Test
    void get_findByAccommodation_shouldReturn200_andPageOfComments() throws Exception {
        // Arrange
        CommentDTO comment1 = new CommentDTO(
                1L, 5.0f, "Excelente", LocalDateTime.now(), true, 1L, 10L, 100L, null
        );
        CommentDTO comment2 = new CommentDTO(
                2L, 4.0f, "Muy bien", LocalDateTime.now(), true, 2L, 10L, 101L, null
        );

        Page<CommentDTO> page = new PageImpl<>(List.of(comment1, comment2));
        when(commentService.findByAccommodation(eq(10L), any(Pageable.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/comments/accommodation/{accommodationId}", 10L)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[1].id").value(2));

        verify(commentService, atLeastOnce()).findByAccommodation(eq(10L), any(Pageable.class));
    }

    @Test
    void get_findByAccommodation_whenNoComments_shouldReturn200_andEmptyPage() throws Exception {
        // Arrange
        Page<CommentDTO> emptyPage = new PageImpl<>(List.of());
        when(commentService.findByAccommodation(eq(10L), any(Pageable.class))).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/api/comments/accommodation/{accommodationId}", 10L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0));

        verify(commentService, atLeastOnce()).findByAccommodation(eq(10L), any(Pageable.class));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void get_findByUser_shouldReturn200_andPageOfComments() throws Exception {
        // Arrange
        CommentDTO comment = new CommentDTO(
                1L, 5.0f, "Mi comentario", LocalDateTime.now(), true, 1L, 10L, 100L, null
        );

        Page<CommentDTO> page = new PageImpl<>(List.of(comment));
        when(currentUserService.getCurrentUserId()).thenReturn(100L);
        when(commentService.findByUser(eq(100L), any(Pageable.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/comments/user/me")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].userId").value(100));

        verify(currentUserService, times(1)).getCurrentUserId();
        verify(commentService, atLeastOnce()).findByUser(eq(100L), any(Pageable.class));
    }

    @Test
    @WithMockUser(username = "host@test.com", roles = {"HOST"})
    void put_reply_withValidData_shouldReturn200() throws Exception {
        // Arrange
        when(currentUserService.getCurrentHostId()).thenReturn(50L);
        doNothing().when(commentService).reply(1L, 50L, "Gracias por tu comentario");

        // Act & Assert
        mockMvc.perform(put("/api/comments/{commentId}/reply", 1L)
                        .param("replyText", "Gracias por tu comentario"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Reply added successfully")));

        verify(currentUserService, atLeastOnce()).getCurrentHostId();
        verify(commentService, atLeastOnce()).reply(1L, 50L, "Gracias por tu comentario");
    }

    @Test
    @WithMockUser(username = "host@test.com", roles = {"HOST"})
    void put_reply_whenCommentNotFound_shouldReturn404() throws Exception {
        // Arrange
        when(currentUserService.getCurrentHostId()).thenReturn(50L);
        doThrow(new CommentNotFoundException("Comentario no encontrado"))
                .when(commentService).reply(anyLong(), anyLong(), anyString());

        // Act & Assert
        mockMvc.perform(put("/api/comments/{commentId}/reply", 999L)
                        .param("replyText", "Gracias"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Comentario no encontrado")));
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
    void put_moderate_withValidData_shouldReturn200() throws Exception {
        // Arrange
        doNothing().when(commentService).moderate(1L, true);

        // Act & Assert
        mockMvc.perform(put("/api/comments/{commentId}/moderate", 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Comment moderated successfully")));

        verify(commentService, times(1)).moderate(1L, true);
    }

    @Test
    void put_moderate_withRejected_shouldReturn200() throws Exception {
        // Arrange
        doNothing().when(commentService).moderate(1L, false);

        // Act & Assert
        mockMvc.perform(put("/api/comments/{commentId}/moderate", 1L)
                        .param("approved", "false"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Comment moderated successfully")));

        verify(commentService, times(1)).moderate(1L, false);
    }

    @Test
    void put_moderate_whenCommentNotFound_shouldReturn404() throws Exception {
        // Arrange
        doThrow(new CommentNotFoundException("Comentario no encontrado"))
                .when(commentService).moderate(anyLong(), anyBoolean());

        // Act & Assert
        mockMvc.perform(put("/api/comments/{commentId}/moderate", 999L)
                        .param("approved", "true"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Comentario no encontrado")));
    }

    @Test
    void delete_delete_shouldReturn204() throws Exception {
        // Arrange
        doNothing().when(commentService).delete(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/comments/{commentId}", 1L))
                .andExpect(status().isNoContent());

        verify(commentService, times(1)).delete(1L);
    }

    @Test
    void delete_delete_whenCommentNotFound_shouldReturn404() throws Exception {
        // Arrange
        doThrow(new CommentNotFoundException("Comentario no encontrado"))
                .when(commentService).delete(anyLong());

        // Act & Assert
        mockMvc.perform(delete("/api/comments/{commentId}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Comentario no encontrado")));
    }

    @Test
    void delete_delete_whenNotOwner_shouldReturn403() throws Exception {
        // Arrange
        doThrow(new CommentForbiddenException("No tienes permiso para eliminar este comentario"))
                .when(commentService).delete(anyLong());

        // Act & Assert
        mockMvc.perform(delete("/api/comments/{commentId}", 1L))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("Operación prohibida en comentarios")));
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        CommentService commentService() {
            return mock(CommentService.class);
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