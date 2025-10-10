package com.avanzada.alojamientos.controllers;

import com.avanzada.alojamientos.DTO.model.PaymentMethod;
import com.avanzada.alojamientos.DTO.model.PaymentStatus;
import com.avanzada.alojamientos.DTO.other.PaymentDTO;
import com.avanzada.alojamientos.controllers.error.ControllerAdvice;
import com.avanzada.alojamientos.security.CurrentUserService;
import com.avanzada.alojamientos.security.CustomUserDetailsService;
import com.avanzada.alojamientos.security.JwtAuthenticationFilter;
import com.avanzada.alojamientos.services.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers = PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ControllerAdvice.class, PaymentControllerTest.TestConfig.class})
class PaymentControllerTest {

    @Resource
    private MockMvc mockMvc;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private PaymentService paymentService;

    @Resource
    private CurrentUserService currentUserService;

    private PaymentDTO paymentDTO;

    @BeforeEach
    void setUp() {
        reset(paymentService, currentUserService);

        paymentDTO = new PaymentDTO(
                1L,
                10L,
                new BigDecimal("250000.50"),
                PaymentMethod.CARD,
                PaymentStatus.PENDING,
                null
        );

        when(currentUserService.getCurrentUserEmail()).thenReturn("user@example.com");
        when(currentUserService.getCurrentUserId()).thenReturn(5L);
    }

    // ===========================================================
    // POST /api/payments
    // ===========================================================
    @Test
    @DisplayName("POST /api/payments - debe registrar un pago exitosamente")
    void register_shouldReturnCreatedPayment() throws Exception {
        when(paymentService.register(any(PaymentDTO.class))).thenReturn(paymentDTO);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.reservationId").value(10))
                .andExpect(jsonPath("$.amount").value(250000.50))
                .andExpect(jsonPath("$.method").value("CARD"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(paymentService, times(1)).register(any(PaymentDTO.class));
    }

    @Test
    @DisplayName("POST /api/payments - debe manejar error en registro de pago")
    void register_shouldHandleError() throws Exception {
        when(paymentService.register(any(PaymentDTO.class)))
                .thenThrow(new RuntimeException("Error al registrar pago"));

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error al registrar pago")));

        verify(paymentService, times(1)).register(any(PaymentDTO.class));
    }

    // ===========================================================
    // GET /api/payments/{id}
    // ===========================================================
    @Test
    @DisplayName("GET /api/payments/{id} - debe retornar un pago existente")
    void findById_shouldReturnPayment() throws Exception {
        when(paymentService.findById(1L)).thenReturn(Optional.of(paymentDTO));

        mockMvc.perform(get("/api/payments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.reservationId").value(10));

        verify(paymentService, times(1)).findById(1L);
    }

    @Test
    @DisplayName("GET /api/payments/{id} - debe retornar null (200) si no existe el pago")
    void findById_shouldReturnNull() throws Exception {
        when(paymentService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/payments/99"))
                .andExpect(status().isOk())          // porque no lanza excepción
                .andExpect(content().string("null")); // el Optional.empty() se serializa como literal null

        verify(paymentService, times(1)).findById(99L);
    }

    // ===========================================================
    // GET /api/payments/reservation/{reservationId}
    // ===========================================================
    @Test
    @DisplayName("GET /api/payments/reservation/{id} - debe retornar lista de pagos")
    void findByReservation_shouldReturnList() throws Exception {
        when(paymentService.findByReservation(10L)).thenReturn(List.of(paymentDTO));

        mockMvc.perform(get("/api/payments/reservation/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].reservationId").value(10));

        verify(paymentService, times(1)).findByReservation(10L);
    }

    @Test
    @DisplayName("GET /api/payments/reservation/{id} - debe retornar lista vacía")
    void findByReservation_shouldReturnEmptyList() throws Exception {
        when(paymentService.findByReservation(10L)).thenReturn(List.of());

        mockMvc.perform(get("/api/payments/reservation/10"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(paymentService, times(1)).findByReservation(10L);
    }

    // ===========================================================
    // PUT /api/payments/{id}/confirm
    // ===========================================================
    @Test
    @DisplayName("PUT /api/payments/{id}/confirm - debe confirmar un pago exitosamente")
    void confirmPayment_shouldReturnConfirmedPayment() throws Exception {
        PaymentDTO confirmed = new PaymentDTO(
                1L,
                10L,
                new BigDecimal("250000.50"),
                PaymentMethod.CARD,
                PaymentStatus.COMPLETED,
                "2025-10-10T14:30:00"
        );

        when(paymentService.confirmPayment(1L)).thenReturn(confirmed);

        mockMvc.perform(put("/api/payments/1/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.paidAt").value("2025-10-10T14:30:00"));

        verify(paymentService, times(1)).confirmPayment(1L);
    }

    @Test
    @DisplayName("PUT /api/payments/{id}/confirm - debe manejar error al confirmar pago")
    void confirmPayment_shouldHandleError() throws Exception {
        when(paymentService.confirmPayment(1L))
                .thenThrow(new RuntimeException("Error al confirmar"));

        mockMvc.perform(put("/api/payments/1/confirm"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error al confirmar")));

        verify(paymentService, times(1)).confirmPayment(1L);
    }

    // ===========================================================
    // Configuración de beans mockeados (estilo estándar)
    // ===========================================================
    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        PaymentService paymentService() {
            return Mockito.mock(PaymentService.class);
        }

        @Bean
        @Primary
        CurrentUserService currentUserService() {
            return Mockito.mock(CurrentUserService.class);
        }

        @Bean
        @Primary
        JwtAuthenticationFilter jwtAuthenticationFilter() {
            return Mockito.mock(JwtAuthenticationFilter.class);
        }

        @Bean
        @Primary
        CustomUserDetailsService customUserDetailsService() {
            return Mockito.mock(CustomUserDetailsService.class);
        }
    }
}
