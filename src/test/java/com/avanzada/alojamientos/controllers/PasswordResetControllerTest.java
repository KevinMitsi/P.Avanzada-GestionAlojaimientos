package com.avanzada.alojamientos.controllers;

import com.avanzada.alojamientos.DTO.auth.PasswordResetDto;
import com.avanzada.alojamientos.DTO.auth.PasswordResetRequestDTO;
import com.avanzada.alojamientos.controllers.error.ControllerAdvice;
import com.avanzada.alojamientos.exceptions.RecoveryTokenException;
import com.avanzada.alojamientos.exceptions.UserNotFoundException;
import com.avanzada.alojamientos.security.CustomUserDetailsService;
import com.avanzada.alojamientos.security.JwtAuthenticationFilter;
import com.avanzada.alojamientos.services.PasswordResetService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PasswordResetController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ControllerAdvice.class, PasswordResetControllerTest.TestConfig.class})
class PasswordResetControllerTest {

    @Resource
    private MockMvc mockMvc;

    @Resource
    private PasswordResetService passwordResetService;

    @BeforeEach
    void setUp() {
        reset(passwordResetService);
    }

    @Test
    void post_requestReset_withValidEmail_shouldReturn200() throws Exception {
        // Arrange
        String validJson = """
            {
              "email": "user@example.com"
            }
            """;

        doNothing().when(passwordResetService).requestReset(any(PasswordResetRequestDTO.class));

        // Act & Assert
        mockMvc.perform(post("/api/password-reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("If the email exists, a recovery code has been sent")));

        verify(passwordResetService, times(1)).requestReset(any(PasswordResetRequestDTO.class));
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "Invalid email format, not-an-email",
            "Blank email, ''",
            "Missing email, "
    })
    void post_requestReset_withInvalidEmail_shouldReturn400(String testName, String email) throws Exception {
        // Arrange
        String invalidJson = email == null
            ? "{}"
            : String.format("{\"email\": \"%s\"}", email);

        // Act & Assert
        mockMvc.perform(post("/api/password-reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Error de validación")));

        verify(passwordResetService, never()).requestReset(any(PasswordResetRequestDTO.class));
    }

    @Test
    void post_requestReset_whenUserNotFound_shouldReturn200_forSecurity() throws Exception {
        // Arrange - Por seguridad, siempre retorna 200 aunque el usuario no exista
        String validJson = """
            {
              "email": "nonexistent@example.com"
            }
            """;

        doThrow(new UserNotFoundException("Usuario no encontrado"))
                .when(passwordResetService).requestReset(any(PasswordResetRequestDTO.class));

        // Act & Assert - El controlador no debe exponer si el usuario existe o no
        mockMvc.perform(post("/api/password-reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Usuario no encontrado")));
    }

    @Test
    void put_resetPassword_withValidData_shouldReturn200() throws Exception {
        // Arrange
        String validJson = """
            {
              "token": "valid-reset-token-123",
              "newPassword": "NewPassword123!"
            }
            """;

        doNothing().when(passwordResetService).resetPassword(any(PasswordResetDto.class));

        // Act & Assert
        mockMvc.perform(put("/api/password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Password has been successfully reset")));

        verify(passwordResetService, times(1)).resetPassword(any(PasswordResetDto.class));
    }

    @Test
    void put_resetPassword_withInvalidToken_shouldReturn400() throws Exception {
        // Arrange
        String validJson = """
            {
              "token": "invalid-token",
              "newPassword": "NewPassword123!"
            }
            """;

        doThrow(new RecoveryTokenException("Token inválido o expirado"))
                .when(passwordResetService).resetPassword(any(PasswordResetDto.class));

        // Act & Assert
        mockMvc.perform(put("/api/password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Error con el token de recuperación")));
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "Password too short, Pass1!, contraseña muy corta (menos de 8 caracteres)",
            "Password too long, VeryLongPassword123!@#$%, contraseña muy larga (más de 20 caracteres)",
            "Missing uppercase, password123!, contraseña sin mayúscula",
            "Missing number, Password!@#, contraseña sin número",
            "Missing symbol, Password123, contraseña sin símbolo",
            "Blank token, Password123!, token vacío con password válido",
            "Blank password, '', contraseña vacía"
    })
    void put_resetPassword_withInvalidPasswordOrToken_shouldReturn400(String testName, String password) throws Exception {
        // Arrange: Determinar el token basado en el caso de prueba
        String token = testName.equals("Blank token") ? "" : "valid-token";
        String finalPassword = testName.equals("Blank password") ? "" : password;

        String invalidJson = String.format("""
            {
              "token": "%s",
              "newPassword": "%s"
            }
            """, token, finalPassword);

        // Act & Assert
        mockMvc.perform(put("/api/password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Error de validación")));

        verify(passwordResetService, never()).resetPassword(any(PasswordResetDto.class));
    }

    @Test
    void put_resetPassword_whenTokenExpired_shouldReturn400() throws Exception {
        // Arrange
        String validJson = """
            {
              "token": "expired-token",
              "newPassword": "NewPassword123!"
            }
            """;

        doThrow(new RecoveryTokenException("El token ha expirado"))
                .when(passwordResetService).resetPassword(any(PasswordResetDto.class));

        // Act & Assert
        mockMvc.perform(put("/api/password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Error con el token de recuperación")));
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        PasswordResetService passwordResetService() {
            return mock(PasswordResetService.class);
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

