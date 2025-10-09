package com.avanzada.alojamientos.controllers;

import com.avanzada.alojamientos.DTO.other.FavoriteAccommodationDTO;
import com.avanzada.alojamientos.DTO.other.FavoriteDTO;
import com.avanzada.alojamientos.controllers.error.ControllerAdvice;
import com.avanzada.alojamientos.exceptions.AccommodationNotFoundException;
import com.avanzada.alojamientos.exceptions.FavoriteAlreadyExistsException;
import com.avanzada.alojamientos.exceptions.FavoriteNotFoundException;
import com.avanzada.alojamientos.security.CurrentUserService;
import com.avanzada.alojamientos.security.CustomUserDetailsService;
import com.avanzada.alojamientos.security.JwtAuthenticationFilter;
import com.avanzada.alojamientos.services.FavoriteService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = FavoriteController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ControllerAdvice.class, FavoriteControllerTest.TestConfig.class})
class FavoriteControllerTest {

    @Resource
    private MockMvc mockMvc;

    @Resource
    private FavoriteService favoriteService;

    @Resource
    private CurrentUserService currentUserService;

    @BeforeEach
    void setUp() {
        reset(favoriteService, currentUserService);
    }

    @Test
    void post_add_withValidAccommodation_shouldReturn200_andFavorite() throws Exception {
        // Arrange
        FavoriteAccommodationDTO accommodationDTO = new FavoriteAccommodationDTO(
                10L,
                "Hermoso apartamento",
                null,
                "Calle 123",
                new BigDecimal("100.00"),
                4,
                4.5,
                List.of()
        );

        FavoriteDTO response = new FavoriteDTO(
                1L,
                100L,
                accommodationDTO,
                "2025-01-01T00:00:00Z"
        );

        when(currentUserService.getCurrentUserId()).thenReturn(100L);
        when(favoriteService.add(100L, 10L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/favorites/{accommodationId}", 10L))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(100))
                .andExpect(jsonPath("$.accommodation.id").value(10))
                .andExpect(jsonPath("$.accommodation.title").value("Hermoso apartamento"));

        verify(currentUserService, times(1)).getCurrentUserId();
        verify(favoriteService, times(1)).add(100L, 10L);
    }

    @Test
    void post_add_whenAccommodationNotFound_shouldReturn404() throws Exception {
        // Arrange
        when(currentUserService.getCurrentUserId()).thenReturn(100L);
        when(favoriteService.add(anyLong(), anyLong()))
                .thenThrow(new AccommodationNotFoundException("Alojamiento no encontrado"));

        // Act & Assert
        mockMvc.perform(post("/api/favorites/{accommodationId}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Alojamiento no encontrado")));
    }

    @Test
    void post_add_whenFavoriteAlreadyExists_shouldReturn409() throws Exception {
        // Arrange
        when(currentUserService.getCurrentUserId()).thenReturn(100L);
        when(favoriteService.add(anyLong(), anyLong()))
                .thenThrow(new FavoriteAlreadyExistsException("El favorito ya existe"));

        // Act & Assert
        mockMvc.perform(post("/api/favorites/{accommodationId}", 10L))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("El favorito ya existe")));
    }

    @Test
    void delete_remove_withValidFavorite_shouldReturn204() throws Exception {
        // Arrange
        doNothing().when(favoriteService).remove(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/favorites/{favoriteId}", 1L))
                .andExpect(status().isNoContent());

        verify(favoriteService, times(1)).remove(1L);
    }

    @Test
    void delete_remove_whenFavoriteNotFound_shouldReturn404() throws Exception {
        // Arrange
        doThrow(new FavoriteNotFoundException("Favorito no encontrado"))
                .when(favoriteService).remove(anyLong());

        // Act & Assert
        mockMvc.perform(delete("/api/favorites/{favoriteId}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Favorito no encontrado")));
    }

    @Test
    void get_findByUser_shouldReturn200_andListOfFavorites() throws Exception {
        // Arrange
        FavoriteAccommodationDTO accommodation1 = new FavoriteAccommodationDTO(
                10L, "Apartamento 1", null, "Calle 123", new BigDecimal("100.00"), 4, 4.5, List.of()
        );
        FavoriteAccommodationDTO accommodation2 = new FavoriteAccommodationDTO(
                11L, "Apartamento 2", null, "Calle 456", new BigDecimal("150.00"), 2, 4.8, List.of()
        );

        FavoriteDTO favorite1 = new FavoriteDTO(1L, 100L, accommodation1, "2025-01-01T00:00:00Z");
        FavoriteDTO favorite2 = new FavoriteDTO(2L, 100L, accommodation2, "2025-01-02T00:00:00Z");

        when(currentUserService.getCurrentUserId()).thenReturn(100L);
        when(favoriteService.findByUser(100L)).thenReturn(List.of(favorite1, favorite2));

        // Act & Assert
        mockMvc.perform(get("/api/favorites/user/me"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].accommodation.title").value("Apartamento 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].accommodation.title").value("Apartamento 2"));

        verify(currentUserService, times(1)).getCurrentUserId();
        verify(favoriteService, times(1)).findByUser(100L);
    }

    @Test
    void get_findByUser_whenNoFavorites_shouldReturn200_andEmptyList() throws Exception {
        // Arrange
        when(currentUserService.getCurrentUserId()).thenReturn(100L);
        when(favoriteService.findByUser(100L)).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/favorites/user/me"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(currentUserService, times(1)).getCurrentUserId();
        verify(favoriteService, times(1)).findByUser(100L);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        FavoriteService favoriteService() {
            return mock(FavoriteService.class);
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