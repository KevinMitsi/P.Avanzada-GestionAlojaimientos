package com.avanzada.alojamientos.controllers;


import com.avanzada.alojamientos.DTO.accommodation.AccommodationDTO;
import com.avanzada.alojamientos.DTO.accommodation.UpdateAccommodationDTO;
import com.avanzada.alojamientos.controllers.error.ControllerAdvice;
import com.avanzada.alojamientos.security.CurrentUserService;
import com.avanzada.alojamientos.security.CustomUserDetailsService;
import com.avanzada.alojamientos.security.JwtAuthenticationFilter;
import com.avanzada.alojamientos.services.AccommodationService;
import com.avanzada.alojamientos.DTO.accommodation.CreateAccommodationDTO;
import com.avanzada.alojamientos.DTO.accommodation.CreateAccommodationResponseDTO;
import com.avanzada.alojamientos.DTO.accommodation.AccommodationMetrics;
import com.avanzada.alojamientos.services.impl.AccommodationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;

import jakarta.annotation.Resource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AccommodationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ControllerAdvice.class, AccommodationControllerTest.TestConfig.class})
class AccommodationControllerTest {

    @Resource
    private MockMvc mockMvc;

    @Resource
    private AccommodationService accommodationService;

    @Resource
    private CurrentUserService currentUserService;

    @BeforeEach
    void setUp() {
        reset(accommodationService, currentUserService);
    }

    @Test
    void put_update_withInvalidBody_shouldReturn400_andNotCallService() throws Exception {
        // Arrange: cuerpo inválido para UpdateAccommodationDTO
        String invalidJson = """
            {
              "title": "abc",
              "description": "",
              "address": "ok",
              "coordinates": null,
              "pricePerNight": null,
              "services": ["wifi"],
              "maxGuests": 0,
              "active": true
            }
            """;

        // Act & Assert
        mockMvc.perform(
                        put("/api/accommodations/{id}", 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidJson)
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString("Error de validación")))
        ;

        verify(accommodationService, never()).update(anyLong(), any(UpdateAccommodationDTO.class));
    }

    @Test
    void put_update_withValidBody_shouldReturn200() throws Exception {
        // Arrange: JSON válido
        String validJson = """
            {
              "title": "A valid title",
              "description": "A valid description",
              "address": "Any address",
              "coordinates": null,
              "pricePerNight": 120.50,
              "services": ["wifi"],
              "maxGuests": 2,
              "active": true
            }
            """;

        when(accommodationService.update(anyLong(), any(UpdateAccommodationDTO.class)))
                .thenReturn( null); // no necesitamos body

        // Act & Assert
        mockMvc.perform(
                        put("/api/accommodations/{id}", 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validJson)
                )
                .andExpect(status().isOk());
    }

    @Test
    void get_metrics_withInvalidDateParam_shouldReturn400_fromControllerAdvice() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/accommodations/{id}/metrics", 5L)
                        .queryParam("start", "2025-13-40")
                        .queryParam("end", "2025-01-10"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("formato inválido")));
    }

    @Test
    void get_findById_withTypeMismatch_shouldReturn400_fromControllerAdvice() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/accommodations/{id}", "not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("formato inválido")));
    }

    @Test
    void get_findByHost_isSecuredNormally_butHereWeFocusOnValidationAndSkipFilters() throws Exception {
        // Arrange
        when(currentUserService.getCurrentHostId()).thenReturn(10L);
        when(accommodationService.findByHost(anyLong(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        // Act & Assert
        mockMvc.perform(get("/api/accommodations/host/me"))
                .andExpect(status().isOk());
    }

    @Test
    void get_findById_whenServiceThrowsIllegalState_shouldMapTo409_byControllerAdvice() throws Exception {
        // Arrange
        when(accommodationService.findById(1L)).thenThrow(new IllegalStateException("bad state"));

        // Act & Assert
        mockMvc.perform(get("/api/accommodations/{id}", 1L))
                .andExpect(status().isConflict());
    }

    @Test
    void get_findById_permitAll_shouldReturn404WithoutAuthWhenNotFound() throws Exception {
        // Arrange
        when(accommodationService.findById(7L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/accommodations/{id}", 7L))
                .andExpect(status().isNotFound());
    }

    // --- Nuevos tests para cobertura completa ---

    @Test
    void post_create_withInvalidBody_shouldReturn400_andNotCallService() throws Exception {
        String invalidJson = """
            {
              "title": "abc",
              "description": "",
              "city": null,
              "coordinates": null,
              "address": "",
              "pricePerNight": null,
              "services": ["wifi"],
              "maxGuests": 0
            }
            """;

        mockMvc.perform(post("/api/accommodations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Error de validación")));

        verify(accommodationService, never()).create(any(CreateAccommodationDTO.class), anyLong());
    }

    @Test
    void post_create_withValidBody_shouldReturn201_andCallService() throws Exception {
        String validJson = """
            {
              "title": "A nice title",
              "description": "A long enough description",
              "city": 1,
              "coordinates": {"lat": -12.0, "lng": -77.0},
              "address": "Street 1",
              "pricePerNight": 99.99,
              "services": ["wifi"],
              "maxGuests": 2
            }
            """;

        when(currentUserService.getCurrentHostId()).thenReturn(22L);
        when(accommodationService.create(any(CreateAccommodationDTO.class), eq(22L)))
                .thenReturn(new CreateAccommodationResponseDTO(
                        1L, 22L, "A nice title", "A long enough description",
                        null, "Street 1", null, new BigDecimal("99.99"), List.of("wifi"), 2, "2025-01-01T00:00:00Z"
                ));

        mockMvc.perform(post("/api/accommodations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isCreated());

        verify(currentUserService, atLeastOnce()).getCurrentHostId();
        verify(accommodationService, times(1)).create(any(CreateAccommodationDTO.class), eq(22L));
    }

    @Test
    void get_search_withDates_shouldReturn200_andCallService() throws Exception {
        Page<AccommodationDTO> empty = new PageImpl<>(List.of());
        when(accommodationService.search(any(), any(Pageable.class))).thenReturn(empty);

        mockMvc.perform(get("/api/accommodations/search")
                        .queryParam("cityId", "5")
                        .queryParam("startDate", "2025-01-10")
                        .queryParam("endDate", "2025-02-01")
                        .queryParam("guests", "3")
                        .queryParam("minPrice", "50")
                        .queryParam("maxPrice", "200")
                        .queryParam("services", "wifi", "pool"))
                .andExpect(status().isOk());

        verify(accommodationService, atLeastOnce()).search(any(), any(Pageable.class));
    }

    @Test
    void get_search_withoutDates_shouldReturn200_andCallService() throws Exception {
        when(accommodationService.search(any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/accommodations/search")
                        .queryParam("cityId", "5")
                        .queryParam("guests", "2"))
                .andExpect(status().isOk());

        verify(accommodationService, atLeastOnce()).search(any(), any(Pageable.class));
    }

    @Test
    void delete_delete_shouldReturn204_andCallService() throws Exception {
        doNothing().when(accommodationService).delete(9L);

        mockMvc.perform(delete("/api/accommodations/{id}", 9L))
                .andExpect(status().isNoContent());

        verify(accommodationService).delete(9L);
    }

    @Test
    void get_metrics_withValidDates_shouldReturn200_andCallService() throws Exception {
        when(accommodationService.getMetrics(eq(3L), any(), any()))
                .thenReturn(new AccommodationMetrics(0L, 0.0, BigDecimal.ZERO));

        mockMvc.perform(get("/api/accommodations/{id}/metrics", 3L)
                        .queryParam("start", "2025-01-01")
                        .queryParam("end", "2025-01-31"))
                .andExpect(status().isOk());

        verify(accommodationService).getMetrics(eq(3L), any(), any());
    }

    @Test
    void get_metrics_withoutDates_shouldReturn200_andCallService() throws Exception {
        when(accommodationService.getMetrics(eq(4L), isNull(), isNull()))
                .thenReturn(new AccommodationMetrics(10L, 4.5, new BigDecimal("123.45")));

        mockMvc.perform(get("/api/accommodations/{id}/metrics", 4L))
                .andExpect(status().isOk());

        verify(accommodationService).getMetrics(eq(4L), isNull(), isNull());
    }

    @Test
    void post_uploadImages_shouldReturn200_andCallServiceImpl() throws Exception {
        // Asegurar que el bean es un mock de la implementación concreta para que el cast en el controller funcione
        AccommodationServiceImpl serviceImplMock = (AccommodationServiceImpl) accommodationService;
        when(serviceImplMock.uploadAndAddImages(eq(11L), anyList(), eq(true)))
                .thenReturn(List.of("http://cdn/x1.jpg", "http://cdn/x2.jpg"));

        MockMultipartFile file1 = new MockMultipartFile("images", "a.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{1,2});
        MockMultipartFile file2 = new MockMultipartFile("images", "b.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{3,4});

        mockMvc.perform(multipart("/api/accommodations/{id}/images/upload", 11L)
                        .file(file1)
                        .file(file2)
                        .param("primary", "true"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("x1.jpg")));

        verify(serviceImplMock).uploadAndAddImages(eq(11L), anyList(), eq(true));
    }

    @Test
    void delete_deleteImage_shouldReturn204_andCallServiceImpl() throws Exception {
        AccommodationServiceImpl serviceImplMock = (AccommodationServiceImpl) accommodationService;
        doNothing().when(serviceImplMock).deleteImageFromCloudinary(12L, 99L);

        mockMvc.perform(delete("/api/accommodations/{accommodationId}/images/{imageId}", 12L, 99L))
                .andExpect(status().isNoContent());

        verify(serviceImplMock).deleteImageFromCloudinary(12L, 99L);
    }

    @Test
    void get_findById_found_shouldReturn200() throws Exception {
        AccommodationDTO dto = new AccommodationDTO(
                1L, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null
        );
        when(accommodationService.findById(1L)).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/api/accommodations/{id}", 1L))
                .andExpect(status().isOk());

        verify(accommodationService, atLeastOnce()).findById(1L);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        AccommodationService accommodationService() {
            // Devolvemos un mock de la implementación concreta para soportar el cast en el controller
            return mock(AccommodationServiceImpl.class);
        }

        @Bean
        @Primary
        CurrentUserService currentUserService() {
            return mock(CurrentUserService.class);
        }

        // Se definen para evitar dependencias si el contexto las requiere.
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

