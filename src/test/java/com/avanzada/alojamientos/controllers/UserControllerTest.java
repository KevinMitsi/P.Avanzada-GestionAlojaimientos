package com.avanzada.alojamientos.controllers;


import com.avanzada.alojamientos.DTO.model.UserRole;
import com.avanzada.alojamientos.DTO.user.EditUserDTO;
import com.avanzada.alojamientos.DTO.user.UserDTO;
import com.avanzada.alojamientos.controllers.error.ControllerAdvice;
import com.avanzada.alojamientos.exceptions.DeletingStorageException;
import com.avanzada.alojamientos.exceptions.InvalidPasswordException;
import com.avanzada.alojamientos.exceptions.UploadingStorageException;
import com.avanzada.alojamientos.exceptions.UserNotFoundException;
import com.avanzada.alojamientos.security.CurrentUserService;
import com.avanzada.alojamientos.security.CustomUserDetailsService;
import com.avanzada.alojamientos.security.JwtAuthenticationFilter;
import com.avanzada.alojamientos.services.UserDocumentService;
import com.avanzada.alojamientos.services.UserService;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ControllerAdvice.class, UserControllerTest.TestConfig.class})
class UserControllerTest {

    @Resource
    private MockMvc mockMvc;

    @Resource
    private UserService userService;

    @Resource
    private UserDocumentService userDocumentService;

    @Resource
    private CurrentUserService currentUserService;

    @BeforeEach
    void setUp() {
        reset(userService, userDocumentService, currentUserService);
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
    void get_findById_whenFound_shouldReturn200_andUser() throws Exception {
        // Arrange
        UserDTO user = new UserDTO(
                1L, "John Doe", "john@example.com", "1234567890",
                LocalDate.of(1990, 1, 1), Set.of(UserRole.USER),
                null, "User description", null, true, true,
                "2025-01-01T00:00:00Z", "2025-01-01T00:00:00Z", false
        );

        when(userService.findById(1L)).thenReturn(Optional.of(user));

        // Act & Assert
        mockMvc.perform(get("/api/users/admin/{userId}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));

        verify(userService, times(1)).findById(1L);
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
    void get_findById_whenNotFound_shouldReturn200_withEmptyOptional() throws Exception {
        // Arrange
        when(userService.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert - El controlador devuelve Optional directamente
        mockMvc.perform(get("/api/users/admin/{userId}", 999L))
                .andExpect(status().isOk());

        verify(userService, times(1)).findById(999L);
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
    void put_enable_withTrueValue_shouldReturn200() throws Exception {
        // Arrange
        doNothing().when(userService).enable("1", true);

        // Act & Assert
        mockMvc.perform(put("/api/users/{userId}/enable", "1")
                        .param("enable", "true"))
                .andExpect(status().isOk());

        verify(userService, times(1)).enable("1", true);
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
    void put_enable_withFalseValue_shouldReturn200() throws Exception {
        // Arrange
        doNothing().when(userService).enable("1", false);

        // Act & Assert
        mockMvc.perform(put("/api/users/{userId}/enable", "1")
                        .param("enable", "false"))
                .andExpect(status().isOk());

        verify(userService, times(1)).enable("1", false);
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
    void put_enable_whenUserNotFound_shouldReturn404() throws Exception {
        // Arrange
        doThrow(new UserNotFoundException("Usuario no encontrado"))
                .when(userService).enable(anyString(), anyBoolean());

        // Act & Assert
        mockMvc.perform(put("/api/users/{userId}/enable", "999")
                        .param("enable", "true"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Usuario no encontrado")));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void put_editProfile_withValidData_shouldReturn200_andUpdatedUser() throws Exception {
        // Arrange
        String validJson = """
            {
              "name": "Jane Doe",
              "phone": "9876543210",
              "dateBirth": "1995-05-15",
              "description": "Updated description"
            }
            """;

        UserDTO updatedUser = new UserDTO(
                1L, "Jane Doe", "jane@example.com", "9876543210",
                LocalDate.of(1995, 5, 15), Set.of(UserRole.USER),
                null, "Updated description", null, true, true,
                "2025-01-01T00:00:00Z", "2025-01-01T00:00:00Z", false
        );

        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(userService.editProfile(eq(1L), any(EditUserDTO.class))).thenReturn(updatedUser);

        // Act & Assert
        mockMvc.perform(put("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Jane Doe"))
                .andExpect(jsonPath("$.phone").value("9876543210"))
                .andExpect(jsonPath("$.description").value("Updated description"));

        verify(currentUserService, times(1)).getCurrentUserId();
        verify(userService, times(1)).editProfile(eq(1L), any(EditUserDTO.class));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void put_editProfile_withInvalidData_shouldReturn400() throws Exception {
        // Arrange: dateBirth en el futuro, name muy largo
        String invalidJson = """
            {
              "name": "a".repeat(101),
              "phone": "12345678901",
              "dateBirth": "2030-01-01",
              "description": "Valid description"
            }
            """;

        // Act & Assert
        mockMvc.perform(put("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Error de validación")));

        verify(userService, never()).editProfile(anyLong(), any(EditUserDTO.class));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void put_editProfile_withDescriptionTooLong_shouldReturn400() throws Exception {
        // Arrange: description mayor a 500 caracteres
        String longDescription = "a".repeat(501);
        String invalidJson = String.format("""
            {
              "name": "John",
              "phone": "123456",
              "dateBirth": "1990-01-01",
              "description": "%s"
            }
            """, longDescription);

        // Act & Assert
        mockMvc.perform(put("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Error de validación")));

        verify(userService, never()).editProfile(anyLong(), any(EditUserDTO.class));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void delete_deleteProfile_shouldReturn204() throws Exception {
        // Arrange
        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        doNothing().when(userService).deleteProfile(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/users"))
                .andExpect(status().isNoContent());

        verify(currentUserService, times(1)).getCurrentUserId();
        verify(userService, times(1)).deleteProfile(1L);
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void delete_deleteProfile_whenUserNotFound_shouldReturn404() throws Exception {
        // Arrange
        when(currentUserService.getCurrentUserId()).thenReturn(999L);
        doThrow(new UserNotFoundException("Usuario no encontrado"))
                .when(userService).deleteProfile(anyLong());

        // Act & Assert
        mockMvc.perform(delete("/api/users"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Usuario no encontrado")));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void put_changePassword_withValidData_shouldReturn202() throws Exception {
        // Arrange
        String validJson = """
            {
              "currentPassword": "OldPassword123!",
              "newPassword": "NewPassword123!"
            }
            """;

        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        doNothing().when(userService).changePassword(1L, "OldPassword123!", "NewPassword123!");

        // Act & Assert
        mockMvc.perform(put("/api/users/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isAccepted())
                .andExpect(content().string(containsString("Contraseña cambiada exitosamente")));

        verify(currentUserService, times(1)).getCurrentUserId();
        verify(userService, times(1)).changePassword(1L, "OldPassword123!", "NewPassword123!");
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void put_changePassword_withInvalidNewPassword_shouldReturn400() throws Exception {
        // Arrange: nueva contraseña sin mayúscula
        String invalidJson = """
            {
              "currentPassword": "OldPassword123!",
              "newPassword": "newpassword123!"
            }
            """;

        // Act & Assert
        mockMvc.perform(put("/api/users/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Error de validación")));

        verify(userService, never()).changePassword(anyLong(), anyString(), anyString());
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void put_changePassword_withWrongCurrentPassword_shouldReturn401() throws Exception {
        // Arrange
        String validJson = """
            {
              "currentPassword": "WrongPassword123!",
              "newPassword": "NewPassword123!"
            }
            """;

        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        doThrow(new InvalidPasswordException("Contraseña actual incorrecta"))
                .when(userService).changePassword(anyLong(), anyString(), anyString());

        // Act & Assert
        mockMvc.perform(put("/api/users/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("Credenciales inválidas")));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void post_uploadDocuments_withValidFiles_shouldReturn201_andUrls() throws Exception {
        // Arrange
        MockMultipartFile doc1 = new MockMultipartFile(
                "documents", "doc1.pdf", MediaType.APPLICATION_PDF_VALUE, "PDF content".getBytes()
        );
        MockMultipartFile doc2 = new MockMultipartFile(
                "documents", "doc2.pdf", MediaType.APPLICATION_PDF_VALUE, "PDF content 2".getBytes()
        );

        List<String> uploadedUrls = List.of(
                "https://cloudinary.com/doc1.pdf",
                "https://cloudinary.com/doc2.pdf"
        );

        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(userDocumentService.uploadDocuments(eq(1L), anyList())).thenReturn(uploadedUrls);

        // Act & Assert
        mockMvc.perform(multipart("/api/users/documents")
                        .file(doc1)
                        .file(doc2))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0]").value("https://cloudinary.com/doc1.pdf"))
                .andExpect(jsonPath("$[1]").value("https://cloudinary.com/doc2.pdf"));

        verify(currentUserService, times(1)).getCurrentUserId();
        verify(userDocumentService, times(1)).uploadDocuments(eq(1L), anyList());
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void post_uploadDocuments_whenUploadFails_shouldReturn500() throws Exception {
        // Arrange
        MockMultipartFile doc = new MockMultipartFile(
                "documents", "doc.pdf", MediaType.APPLICATION_PDF_VALUE, "PDF content".getBytes()
        );

        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(userDocumentService.uploadDocuments(eq(1L), anyList()))
                .thenThrow(new UploadingStorageException("Error al subir el documento"));

        // Act & Assert
        mockMvc.perform(multipart("/api/users/documents")
                        .file(doc))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Error al subir la imagen")));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void delete_deleteDocument_shouldReturn204() throws Exception {
        // Arrange
        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        doNothing().when(userDocumentService).deleteDocument(1L, 0L);

        // Act & Assert
        mockMvc.perform(delete("/api/users/documents/{documentIndex}", 0L))
                .andExpect(status().isNoContent());

        verify(currentUserService, times(1)).getCurrentUserId();
        verify(userDocumentService, times(1)).deleteDocument(1L, 0L);
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void delete_deleteDocument_whenDeletionFails_shouldReturn500() throws Exception {
        // Arrange
        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        doThrow(new DeletingStorageException("Error al eliminar el documento"))
                .when(userDocumentService).deleteDocument(anyLong(), anyLong());

        // Act & Assert
        mockMvc.perform(delete("/api/users/documents/{documentIndex}", 0L))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Error al eliminar la imagen")));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void post_uploadProfileImage_withValidFile_shouldReturn201_andUrl() throws Exception {
        // Arrange
        MockMultipartFile image = new MockMultipartFile(
                "image", "profile.jpg", MediaType.IMAGE_JPEG_VALUE, "Image content".getBytes()
        );

        String imageUrl = "https://cloudinary.com/profile.jpg";

        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(userService.uploadProfileImage(eq(1L), any(MultipartFile.class))).thenReturn(imageUrl);

        // Act & Assert
        mockMvc.perform(multipart("/api/users/profile-image")
                        .file(image))
                .andExpect(status().isCreated())
                .andExpect(content().string(imageUrl));

        verify(currentUserService, times(1)).getCurrentUserId();
        verify(userService, times(1)).uploadProfileImage(eq(1L), any(MultipartFile.class));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void post_uploadProfileImage_whenUploadFails_shouldReturn500() throws Exception {
        // Arrange
        MockMultipartFile image = new MockMultipartFile(
                "image", "profile.jpg", MediaType.IMAGE_JPEG_VALUE, "Image content".getBytes()
        );

        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(userService.uploadProfileImage(eq(1L), any(MultipartFile.class)))
                .thenThrow(new UploadingStorageException("Error al subir la imagen"));

        // Act & Assert
        mockMvc.perform(multipart("/api/users/profile-image")
                        .file(image))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Error al subir la imagen")));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void delete_deleteProfileImage_shouldReturn204() throws Exception {
        // Arrange
        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        doNothing().when(userService).deleteProfileImage(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/users/profile-image"))
                .andExpect(status().isNoContent());

        verify(currentUserService, times(1)).getCurrentUserId();
        verify(userService, times(1)).deleteProfileImage(1L);
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void delete_deleteProfileImage_whenDeletionFails_shouldReturn500() throws Exception {
        // Arrange
        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        doThrow(new DeletingStorageException("Error al eliminar la imagen"))
                .when(userService).deleteProfileImage(anyLong());

        // Act & Assert
        mockMvc.perform(delete("/api/users/profile-image"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Error al eliminar la imagen")));
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        UserService userService() {
            return mock(UserService.class);
        }

        @Bean
        @Primary
        UserDocumentService userDocumentService() {
            return mock(UserDocumentService.class);
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

