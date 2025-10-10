package com.avanzada.alojamientos.controllers;

import com.avanzada.alojamientos.DTO.auth.AuthResponseDTO;
import com.avanzada.alojamientos.DTO.auth.LoginRequestDTO;
import com.avanzada.alojamientos.DTO.auth.RegisterUserDTO;
import com.avanzada.alojamientos.DTO.model.UserRole;
import com.avanzada.alojamientos.DTO.user.UserDTO;
import com.avanzada.alojamientos.controllers.error.ControllerAdvice;
import com.avanzada.alojamientos.exceptions.InvalidPasswordException;
import com.avanzada.alojamientos.exceptions.UnauthorizedException;
import com.avanzada.alojamientos.exceptions.UserNotFoundException;
import com.avanzada.alojamientos.security.CustomUserDetailsService;
import com.avanzada.alojamientos.security.JwtAuthenticationFilter;
import com.avanzada.alojamientos.services.AuthService;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ControllerAdvice.class, AuthControllerTest.TestConfig.class})
class AuthControllerTest {

    @Resource
    private MockMvc mockMvc;

    @Resource
    private AuthService authService;

    @BeforeEach
    void setUp() {
        reset(authService);
    }

    @Test
    void post_login_withValidCredentials_shouldReturn200_andToken() throws Exception {
        // Arrange
        String validJson = """
            {
              "email": "user@test.com",
              "password": "password123"
            }
            """;

        AuthResponseDTO response = new AuthResponseDTO(
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test",
                1L,
                "user@test.com",
                "Test User",
                Set.of(UserRole.USER),
                true,
                false
        );

        when(authService.login(any(LoginRequestDTO.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test"))
                .andExpect(jsonPath("$.email").value("user@test.com"))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.userId").value(1));

        verify(authService, times(1)).login(any(LoginRequestDTO.class));
    }

    @Test
    void post_login_withInvalidBody_shouldReturn400() throws Exception {
        // Arrange: email inválido y password vacío
        String invalidJson = """
            {
              "email": "not-an-email",
              "password": ""
            }
            """;

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Error de validación")));

        verify(authService, never()).login(any(LoginRequestDTO.class));
    }

    @Test
    void post_login_withInvalidPassword_shouldReturn401() throws Exception {
        // Arrange
        String validJson = """
            {
              "email": "user@test.com",
              "password": "wrongpassword"
            }
            """;

        when(authService.login(any(LoginRequestDTO.class)))
                .thenThrow(new InvalidPasswordException("Contraseña incorrecta"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("Credenciales inválidas")));
    }

    @Test
    void post_register_withValidBody_shouldReturn200_andToken() throws Exception {
        // Arrange
        String validJson = """
            {
              "email": "newuser@test.com",
              "password": "password123",
              "name": "New User",
              "phone": "1234567890",
              "dateOfBirth": "2000-01-01"
            }
            """;

        AuthResponseDTO response = new AuthResponseDTO(
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.newuser",
                2L,
                "newuser@test.com",
                "New User",
                Set.of(UserRole.USER),
                false,
                false
        );

        when(authService.register(any(RegisterUserDTO.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("newuser@test.com"))
                .andExpect(jsonPath("$.name").value("New User"))
                .andExpect(jsonPath("$.userId").value(2));

        verify(authService, times(1)).register(any(RegisterUserDTO.class));
    }

    @Test
    void post_register_withInvalidBody_shouldReturn400() throws Exception {
        // Arrange: email inválido, password muy corto, nombre vacío, etc.
        String invalidJson = """
            {
              "email": "invalid-email",
              "password": "123",
              "name": "",
              "phone": "12345678901234567890",
              "dateOfBirth": "2030-01-01"
            }
            """;

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Error de validación")));

        verify(authService, never()).register(any(RegisterUserDTO.class));
    }

    @Test
    void post_register_withDuplicateEmail_shouldReturn409() throws Exception {
        // Arrange
        String validJson = """
            {
              "email": "existing@test.com",
              "password": "password123",
              "name": "Existing User",
              "phone": "1234567890",
              "dateOfBirth": "2000-01-01"
            }
            """;

        when(authService.register(any(RegisterUserDTO.class)))
                .thenThrow(new IllegalStateException("El email ya está registrado"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("Estado de operación inválido")));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void get_me_withAuthenticatedUser_shouldReturn200_andUserInfo() throws Exception {
        // Arrange
        UserDTO userDTO = new UserDTO(
                1L,
                "Test User",
                "user@test.com",
                "1234567890",
                LocalDate.of(1990, 1, 1),
                Set.of(UserRole.USER),
                null,
                "User description",
                null,
                true,
                true,
                "2025-01-01T00:00:00Z",
                "2025-01-01T00:00:00Z",
                false
        );

        when(authService.getCurrentUser()).thenReturn(userDTO);

        // Act & Assert
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("user@test.com"))
                .andExpect(jsonPath("$.name").value("Test User"));

        verify(authService, times(1)).getCurrentUser();
    }

    @Test
    void get_me_whenUserNotFound_shouldReturn404() throws Exception {
        // Arrange
        when(authService.getCurrentUser())
                .thenThrow(new UserNotFoundException("Usuario no encontrado"));

        // Act & Assert
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Usuario no encontrado")));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void post_logout_withAuthenticatedUser_shouldReturn200() throws Exception {
        // Arrange
        doNothing().when(authService).logout();

        // Act & Assert
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Logout exitoso")));

        verify(authService, times(1)).logout();
    }

    @Test
    void post_refreshToken_withValidToken_shouldReturn200_andNewToken() throws Exception {
        // Arrange
        AuthResponseDTO response = new AuthResponseDTO(
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.refreshed",
                1L,
                "user@test.com",
                "Test User",
                Set.of(UserRole.USER),
                true,
                false
        );

        when(authService.refreshToken(anyString())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/auth/refresh")
                        .param("refreshToken", "valid-refresh-token"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.refreshed"));

        verify(authService, times(1)).refreshToken("valid-refresh-token");
    }

    @Test
    void post_refreshToken_withInvalidToken_shouldReturn401() throws Exception {
        // Arrange
        when(authService.refreshToken(anyString()))
                .thenThrow(new UnauthorizedException("Token de refresh inválido o expirado"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/refresh")
                        .param("refreshToken", "invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("Error de autorización")));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void put_becomeHost_withAuthenticatedUser_shouldReturn200_andUpdatedUser() throws Exception {
        // Arrange
        UserDTO updatedUser = new UserDTO(
                1L,
                "Test User",
                "user@test.com",
                "1234567890",
                LocalDate.of(1990, 1, 1),
                Set.of(UserRole.USER, UserRole.HOST),
                null,
                "User description",
                null,
                true,
                true,
                "2025-01-01T00:00:00Z",
                "2025-01-01T00:00:00Z",
                false
        );

        when(authService.becomeHost()).thenReturn(updatedUser);

        // Act & Assert
        mockMvc.perform(put("/api/auth/become-host"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.email").value("user@test.com"));

        verify(authService, times(1)).becomeHost();
    }

    @Test
    void put_becomeHost_whenAlreadyHost_shouldReturn409() throws Exception {
        // Arrange
        when(authService.becomeHost())
                .thenThrow(new IllegalStateException("El usuario ya es HOST"));

        // Act & Assert
        mockMvc.perform(put("/api/auth/become-host"))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("Estado de operación inválido")));
    }

    @Test
    void put_becomeHost_whenUserNotFound_shouldReturn404() throws Exception {
        // Arrange
        when(authService.becomeHost())
                .thenThrow(new UserNotFoundException("Usuario no encontrado"));

        // Act & Assert
        mockMvc.perform(put("/api/auth/become-host"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Usuario no encontrado")));
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        AuthService authService() {
            return mock(AuthService.class);
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