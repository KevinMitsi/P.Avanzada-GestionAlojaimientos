package com.avanzada.alojamientos.controllers;

import com.avanzada.alojamientos.DTO.model.ReservationStatus;
import com.avanzada.alojamientos.DTO.reservation.CreateReservationDTO;
import com.avanzada.alojamientos.DTO.reservation.ReservationDTO;
import com.avanzada.alojamientos.DTO.reservation.ReservationSearchCriteria;
import com.avanzada.alojamientos.controllers.error.ControllerAdvice;
import com.avanzada.alojamientos.exceptions.*;
import com.avanzada.alojamientos.security.CurrentUserService;
import com.avanzada.alojamientos.security.CustomUserDetailsService;
import com.avanzada.alojamientos.security.JwtAuthenticationFilter;
import com.avanzada.alojamientos.services.ReservationService;
import jakarta.annotation.Resource;
import org.jetbrains.annotations.NotNull;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ReservationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ControllerAdvice.class, ReservationControllerTest.TestConfig.class})
class ReservationControllerTest {

    @Resource
    private MockMvc mockMvc;

    @Resource
    private ReservationService reservationService;

    @Resource
    private CurrentUserService currentUserService;

    @BeforeEach
    void setUp() {
        reset(reservationService, currentUserService);
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void post_create_withValidBody_shouldReturn200_andReservation() throws Exception {
        // Arrange
        String validJson = """
            {
              "accommodationId": 10,
              "startDate": "2025-12-01",
              "endDate": "2025-12-05",
              "guests": 2
            }
            """;

        ReservationDTO response = new ReservationDTO(
                1L, 10L, 100L, 50L,
                LocalDate.of(2025, 12, 1),
                LocalDate.of(2025, 12, 5),
                4,
                new BigDecimal("400.00"),
                ReservationStatus.PENDING,
                "2025-10-09T00:00:00Z",
                null, null, null, null
        );

        when(currentUserService.getCurrentUserId()).thenReturn(100L);
        when(reservationService.create(eq(100L), any(CreateReservationDTO.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.accommodationId").value(10))
                .andExpect(jsonPath("$.userId").value(100))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalPrice").value(400.00));

        verify(currentUserService, times(1)).getCurrentUserId();
        verify(reservationService, times(1)).create(eq(100L), any(CreateReservationDTO.class));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void post_create_withInvalidBody_shouldReturn400() throws Exception {
        // Arrange: guests = 0, startDate en el pasado
        String invalidJson = """
            {
              "accommodationId": 10,
              "startDate": "2020-01-01",
              "endDate": "2025-12-05",
              "guests": 0
            }
            """;

        // Act & Assert
        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Error de validación")));

        verify(reservationService, never()).create(anyLong(), any(CreateReservationDTO.class));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void post_create_withMissingFields_shouldReturn400() throws Exception {
        // Arrange: falta accommodationId
        String invalidJson = """
            {
              "startDate": "2025-12-01",
              "endDate": "2025-12-05",
              "guests": 2
            }
            """;

        // Act & Assert
        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Error de validación")));

        verify(reservationService, never()).create(anyLong(), any(CreateReservationDTO.class));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void post_create_whenAccommodationNotFound_shouldReturn404() throws Exception {
        // Arrange
        String validJson = """
            {
              "accommodationId": 999,
              "startDate": "2025-12-01",
              "endDate": "2025-12-05",
              "guests": 2
            }
            """;

        when(currentUserService.getCurrentUserId()).thenReturn(100L);
        when(reservationService.create(anyLong(), any(CreateReservationDTO.class)))
                .thenThrow(new AccommodationNotFoundException("Alojamiento no encontrado"));

        // Act & Assert
        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Alojamiento no encontrado")));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void post_create_whenAccommodationNotAvailable_shouldReturn409() throws Exception {
        // Arrange
        String validJson = """
            {
              "accommodationId": 10,
              "startDate": "2025-12-01",
              "endDate": "2025-12-05",
              "guests": 2
            }
            """;

        when(currentUserService.getCurrentUserId()).thenReturn(100L);
        when(reservationService.create(anyLong(), any(CreateReservationDTO.class)))
                .thenThrow(new ReservationAvailabilityException("Alojamiento no disponible para las fechas seleccionadas"));

        // Act & Assert
        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("Alojamiento no disponible")));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void post_create_withInvalidDates_shouldReturn400() throws Exception {
        // Arrange
        String validJson = """
            {
              "accommodationId": 10,
              "startDate": "2025-12-05",
              "endDate": "2025-12-01",
              "guests": 2
            }
            """;

        when(currentUserService.getCurrentUserId()).thenReturn(100L);
        when(reservationService.create(anyLong(), any(CreateReservationDTO.class)))
                .thenThrow(new ReservationValidationException("La fecha de fin debe ser posterior a la fecha de inicio"));

        // Act & Assert
        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Error de validación en la reserva")));
    }

    @Test
    void get_findById_whenFound_shouldReturn200_andReservation() throws Exception {
        // Arrange
        ReservationDTO reservation = new ReservationDTO(
                5L, 10L, 100L, 50L,
                LocalDate.of(2025, 12, 1),
                LocalDate.of(2025, 12, 5),
                4,
                new BigDecimal("400.00"),
                ReservationStatus.CONFIRMED,
                "2025-10-09T00:00:00Z",
                null, null, null, null
        );

        when(reservationService.findById(5L)).thenReturn(Optional.of(reservation));

        // Act & Assert
        mockMvc.perform(get("/api/reservations/{reservationId}", 5L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(reservationService, times(1)).findById(5L);
    }

    @Test
    void get_findById_whenNotFound_shouldReturn200_withEmptyOptional() throws Exception {
        // Arrange
        when(reservationService.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert - El controlador devuelve Optional directamente, Spring lo serializa como null
        mockMvc.perform(get("/api/reservations/{reservationId}", 999L))
                .andExpect(status().isOk());

        verify(reservationService, times(1)).findById(999L);
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void get_findByUser_shouldReturn200_andPageOfReservations() throws Exception {
        Page<ReservationDTO> page = getReservationDTOS();
        when(currentUserService.getCurrentUserId()).thenReturn(100L);
        when(reservationService.findByUser(eq(100L), any(Pageable.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/reservations/user/me")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[1].id").value(2));

        verify(currentUserService, times(1)).getCurrentUserId();
        verify(reservationService, atLeastOnce()).findByUser(eq(100L), any(Pageable.class));
    }

    private static @NotNull Page<ReservationDTO> getReservationDTOS() {
        ReservationDTO reservation1 = new ReservationDTO(
                1L, 10L, 100L, 50L,
                LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 5),
                4, new BigDecimal("400.00"), ReservationStatus.PENDING,
                "2025-10-09T00:00:00Z", null, null, null, null
        );
        ReservationDTO reservation2 = new ReservationDTO(
                2L, 11L, 100L, 51L,
                LocalDate.of(2025, 12, 10), LocalDate.of(2025, 12, 15),
                5, new BigDecimal("500.00"), ReservationStatus.CONFIRMED,
                "2025-10-09T00:00:00Z", null, null, null, null
        );

        return new PageImpl<>(List.of(reservation1, reservation2));
    }

    @Test
    void get_findByAccommodation_shouldReturn200_andPageOfReservations() throws Exception {
        // Arrange
        ReservationDTO reservation = new ReservationDTO(
                1L, 10L, 100L, 50L,
                LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 5),
                4, new BigDecimal("400.00"), ReservationStatus.CONFIRMED,
                "2025-10-09T00:00:00Z", null, null, null, null
        );

        Page<ReservationDTO> page = new PageImpl<>(List.of(reservation));
        when(reservationService.findByAccommodation(eq(10L), any(Pageable.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/reservations/accommodation/{accommodationId}", 10L)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].accommodationId").value(10));

        verify(reservationService, atLeastOnce()).findByAccommodation(eq(10L), any(Pageable.class));
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
    void post_search_withCriteria_shouldReturn200_andPageOfReservations() throws Exception {
        // Arrange
        String searchJson = """
            {
              "userId": 100,
              "status": "CONFIRMED"
            }
            """;

        ReservationDTO reservation = new ReservationDTO(
                1L, 10L, 100L, 50L,
                LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 5),
                4, new BigDecimal("400.00"), ReservationStatus.CONFIRMED,
                "2025-10-09T00:00:00Z", null, null, null, null
        );

        Page<ReservationDTO> page = new PageImpl<>(List.of(reservation));
        when(reservationService.searchReservations(any(ReservationSearchCriteria.class), any(Pageable.class)))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(post("/api/reservations/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(searchJson)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1));

        verify(reservationService, atLeastOnce()).searchReservations(any(ReservationSearchCriteria.class), any(Pageable.class));
    }

    @Test
    void get_isAvailable_whenAvailable_shouldReturn200_andTrue() throws Exception {
        // Arrange
        when(reservationService.isAvailable(10L, LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 5), 2))
                .thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/reservations/availability")
                        .param("accommodationId", "10")
                        .param("start", "2025-12-01")
                        .param("end", "2025-12-05")
                        .param("guests", "2"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(reservationService, times(1)).isAvailable(10L, LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 5), 2);
    }

    @Test
    void get_isAvailable_whenNotAvailable_shouldReturn200_andFalse() throws Exception {
        // Arrange
        when(reservationService.isAvailable(10L, LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 5), 2))
                .thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/reservations/availability")
                        .param("accommodationId", "10")
                        .param("start", "2025-12-01")
                        .param("end", "2025-12-05")
                        .param("guests", "2"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(reservationService, times(1)).isAvailable(10L, LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 5), 2);
    }

    @Test
    void get_calculatePrice_shouldReturn200_andPrice() throws Exception {
        // Arrange
        when(reservationService.calculatePrice(10L, LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 5), 2))
                .thenReturn(new BigDecimal("400.00"));

        // Act & Assert
        mockMvc.perform(get("/api/reservations/price")
                        .param("accommodationId", "10")
                        .param("start", "2025-12-01")
                        .param("end", "2025-12-05")
                        .param("guests", "2"))
                .andExpect(status().isOk())
                .andExpect(content().string("400.00"));

        verify(reservationService, times(1)).calculatePrice(10L, LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 5), 2);
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void put_cancel_withValidData_shouldReturn200() throws Exception {
        // Arrange
        when(currentUserService.getCurrentUserId()).thenReturn(100L);
        doNothing().when(reservationService).cancel(1L, 100L, "Cambio de planes");

        // Act & Assert
        mockMvc.perform(put("/api/reservations/{reservationId}/cancel", 1L)
                        .param("motivo", "Cambio de planes"))
                .andExpect(status().isOk());

        verify(currentUserService, times(1)).getCurrentUserId();
        verify(reservationService, times(1)).cancel(1L, 100L, "Cambio de planes");
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void put_cancel_whenReservationNotFound_shouldReturn404() throws Exception {
        // Arrange
        when(currentUserService.getCurrentUserId()).thenReturn(100L);
        doThrow(new ReservationNotFoundException("Reserva no encontrada"))
                .when(reservationService).cancel(anyLong(), anyLong(), anyString());

        // Act & Assert
        mockMvc.perform(put("/api/reservations/{reservationId}/cancel", 999L)
                        .param("motivo", "Cambio de planes"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Reserva no encontrada")));
    }

    @Test
    @WithMockUser(username = "host@test.com", roles = {"HOST"})
    void put_updateStatus_withValidData_shouldReturn200() throws Exception {
        // Arrange
        when(currentUserService.getCurrentHostId()).thenReturn(50L);
        doNothing().when(reservationService).updateStatus(1L, ReservationStatus.CONFIRMED, 50L);

        // Act & Assert
        mockMvc.perform(put("/api/reservations/{reservationId}/status", 1L)
                        .param("status", "CONFIRMED"))
                .andExpect(status().isOk());

        verify(currentUserService, times(1)).getCurrentHostId();
        verify(reservationService, times(1)).updateStatus(1L, ReservationStatus.CONFIRMED, 50L);
    }

    @Test
    void put_updateStatus_whenNotHost_shouldReturn403() throws Exception {
        // Arrange
        when(currentUserService.getCurrentHostId()).thenReturn(50L);
        doThrow(new ReservationPermissionException("Solo el host puede actualizar el estado"))
                .when(reservationService).updateStatus(anyLong(), any(ReservationStatus.class), anyLong());

        // Act & Assert
        mockMvc.perform(put("/api/reservations/{reservationId}/status", 1L)
                        .param("status", "CONFIRMED"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("Permisos insuficientes")));
    }

    @Test
    void put_updateStatus_whenReservationNotFound_shouldReturn404() throws Exception {
        // Arrange
        when(currentUserService.getCurrentHostId()).thenReturn(50L);
        doThrow(new ReservationNotFoundException("Reserva no encontrada"))
                .when(reservationService).updateStatus(anyLong(), any(ReservationStatus.class), anyLong());

        // Act & Assert
        mockMvc.perform(put("/api/reservations/{reservationId}/status", 999L)
                        .param("status", "CONFIRMED"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Reserva no encontrada")));
    }

    @Test
    void put_updateStatus_withInvalidTransition_shouldReturn409() throws Exception {
        // Arrange
        when(currentUserService.getCurrentHostId()).thenReturn(50L);
        doThrow(new ReservationStateException("No se puede cambiar de COMPLETED a PENDING"))
                .when(reservationService).updateStatus(anyLong(), any(ReservationStatus.class), anyLong());

        // Act & Assert
        mockMvc.perform(put("/api/reservations/{reservationId}/status", 1L)
                        .param("status", "PENDING"))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("Estado de reserva inválido")));
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        ReservationService reservationService() {
            return mock(ReservationService.class);
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