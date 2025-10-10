package com.avanzada.alojamientos.controllers;

import com.avanzada.alojamientos.DTO.other.PaymentDTO;
import com.avanzada.alojamientos.controllers.error.ControllerAdvice;
import com.avanzada.alojamientos.security.CustomUserDetailsService;
import com.avanzada.alojamientos.security.JwtAuthenticationFilter;
import com.avanzada.alojamientos.services.MercadoPagoService;
import com.avanzada.alojamientos.services.PaymentService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = MercadoPagoController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ControllerAdvice.class, MercadoPagoControllerTest.TestConfig.class})
class MercadoPagoControllerTest {

    @Resource
    private MockMvc mockMvc;

    @Resource
    private MercadoPagoService mercadoPagoService;

    @Resource
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        reset(mercadoPagoService, paymentService);
    }

    //  CASO 1: Éxito al crear preferencia
    @Test
    @DisplayName("POST /create-preference → 200 OK (creación exitosa)")
    @WithMockUser(username = "user@test.com")
    void createPreference_shouldReturnOk() throws Exception {
        when(mercadoPagoService.createPreference(1L))
                .thenReturn("https://mercadopago.com/pref/123");

        mockMvc.perform(post("/api/mercadopago/create-preference/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("mercadopago.com/pref/123")));
    }

    //  CASO 2: Excepción al crear preferencia
    @Test
    @DisplayName("POST /create-preference → 400 BadRequest (excepción en servicio)")
    void createPreference_shouldReturnBadRequest() throws Exception {
        when(mercadoPagoService.createPreference(1L))
                .thenThrow(new RuntimeException("Error simulado"));

        mockMvc.perform(post("/api/mercadopago/create-preference/{id}", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Error al crear la preferencia")));
    }

    //  CASO 3: createPreference devuelve null
    @Test
    @DisplayName("POST /create-preference → 400 BadRequest (respuesta nula)")
    void createPreference_shouldReturnBadRequestWhenNull() throws Exception {
        when(mercadoPagoService.createPreference(1L)).thenReturn(null);

        mockMvc.perform(post("/api/mercadopago/create-preference/{id}", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Error al crear la preferencia")));
    }

    //  CASO 4: Confirmar pago exitoso
    @Test
    @DisplayName("POST /confirm → 200 OK (confirmación exitosa)")
    void confirmPayment_shouldReturnOk() throws Exception {
        PaymentDTO payment = new PaymentDTO(1L, 1L, BigDecimal.TEN, null, null, null);
        when(paymentService.findByReservation(1L)).thenReturn(List.of(payment));

        mockMvc.perform(post("/api/mercadopago/confirm/{id}", 1L)
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Pago confirmado")));
    }

    //  CASO 5: Pago no aprobado
    @Test
    @DisplayName("POST /confirm → 400 BadRequest (status no COMPLETED)")
    void confirmPayment_shouldReturnBadRequestIfNotCompleted() throws Exception {
        mockMvc.perform(post("/api/mercadopago/confirm/{id}", 1L)
                        .param("status", "PENDING"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Pago no aprobado")));
    }

    //  CASO 6: No hay pagos
    @Test
    @DisplayName("POST /confirm → 400 BadRequest (sin pagos)")
    void confirmPayment_shouldReturnBadRequestIfNoPayments() throws Exception {
        when(paymentService.findByReservation(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/api/mercadopago/confirm/{id}", 1L)
                        .param("status", "COMPLETED"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("No se encontró pago asociado")));
    }

    //  CASO 7: Lista nula
    @Test
    @DisplayName("POST /confirm → 400 BadRequest (lista nula)")
    void confirmPayment_shouldReturnBadRequestIfNullList() throws Exception {
        when(paymentService.findByReservation(1L)).thenReturn(null);

        mockMvc.perform(post("/api/mercadopago/confirm/{id}", 1L)
                        .param("status", "COMPLETED"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("No se encontró pago asociado")));
    }

    //  CASO 8: Error al confirmar pago
    @Test
    @DisplayName("POST /confirm → 400 BadRequest (error interno al confirmar)")
    void confirmPayment_shouldReturnBadRequestOnError() throws Exception {
        PaymentDTO payment = new PaymentDTO(1L, 1L, BigDecimal.TEN, null, null, null);
        when(paymentService.findByReservation(1L)).thenReturn(List.of(payment));
        doThrow(new RuntimeException("Error en confirmación")).when(paymentService).confirmPayment(1L);

        mockMvc.perform(post("/api/mercadopago/confirm/{id}", 1L)
                        .param("status", "COMPLETED"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Error al confirmar el pago")));
    }

    // 🔧 Beans simulados (igual que tus otros controladores)
    @TestConfiguration
    static class TestConfig {

        @Bean
        @Primary
        MercadoPagoService mercadoPagoService() {
            return mock(MercadoPagoService.class);
        }

        @Bean
        @Primary
        PaymentService paymentService() {
            return mock(PaymentService.class);
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
