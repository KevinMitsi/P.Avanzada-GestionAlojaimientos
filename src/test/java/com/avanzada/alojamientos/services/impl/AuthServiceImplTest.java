package com.avanzada.alojamientos.services.impl;

import com.avanzada.alojamientos.DTO.auth.AuthResponseDTO;
import com.avanzada.alojamientos.DTO.auth.LoginRequestDTO;
import com.avanzada.alojamientos.DTO.auth.RegisterUserDTO;
import com.avanzada.alojamientos.DTO.user.UserDTO;
import com.avanzada.alojamientos.DTO.model.UserRole;
import com.avanzada.alojamientos.entities.UserEntity;
import com.avanzada.alojamientos.entities.HostProfileEntity;
import com.avanzada.alojamientos.exceptions.InvalidPasswordException;
import com.avanzada.alojamientos.exceptions.UserNotFoundException;
import com.avanzada.alojamientos.mappers.UserMapper;
import com.avanzada.alojamientos.security.CustomUserDetailsService;
import com.avanzada.alojamientos.security.JwtService;
import com.avanzada.alojamientos.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserService userService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private AuthServiceImpl authService;

    private LoginRequestDTO loginRequest;
    private RegisterUserDTO registerRequest;
    private UserEntity userEntity;
    private UserDTO userDTO;
    private HostProfileEntity hostProfile;

    @BeforeEach
    void setUp() {
        // Arrange - Configuración común para todos los tests
        loginRequest = new LoginRequestDTO("test@example.com", "password123");

        registerRequest = new RegisterUserDTO(
            "newuser@example.com",
            "password123",
            "New User",
            "1234567890",
            LocalDate.of(1990, 1, 1)
        );

        hostProfile = new HostProfileEntity();
        hostProfile.setVerified(true);

        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("test@example.com");
        userEntity.setName("Test User");
        userEntity.setRoles(Set.of(UserRole.USER));
        userEntity.setVerified(true);
        userEntity.setHostProfile(hostProfile);

        userDTO = new UserDTO(
            1L,
            "Test User",
            "test@example.com",
            "1234567890",
            LocalDate.of(1990, 1, 1),
            Set.of(UserRole.USER),
            null,
            null,
            null,
            true,
            true,
            null,
            null,
            false
        );
    }

    @Test
    void login_WithValidCredentials_ShouldReturnAuthResponse() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(userDetailsService.getUserEntityByEmail("test@example.com")).thenReturn(userEntity);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-token");

        // Act
        AuthResponseDTO result = authService.login(loginRequest);

        // Assert
        assertNotNull(result);
        assertEquals("jwt-token", result.token());
        assertEquals("Bearer", result.type());
        assertEquals(1L, result.userId());
        assertEquals("test@example.com", result.email());
        assertEquals("Test User", result.name());
        assertEquals(Set.of(UserRole.USER), result.roles());
        assertTrue(result.isVerified());
        assertTrue(result.isHostVerified());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService).getUserEntityByEmail("test@example.com");
        verify(jwtService).generateToken(userDetails);
    }

    @Test
    void login_WithInvalidCredentials_ShouldThrowInvalidPasswordException() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        InvalidPasswordException exception = assertThrows(
            InvalidPasswordException.class,
            () -> authService.login(loginRequest)
        );

        assertEquals("Credenciales inválidas", exception.getMessage());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(jwtService, userDetailsService);
    }

    @Test
    void login_WithUserWithoutHostProfile_ShouldReturnFalseForHostVerified() {
        // Arrange
        userEntity.setHostProfile(null);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(userDetailsService.getUserEntityByEmail("test@example.com")).thenReturn(userEntity);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-token");

        // Act
        AuthResponseDTO result = authService.login(loginRequest);

        // Assert
        assertFalse(result.isHostVerified());
    }

    @Test
    void login_WithUnverifiedHostProfile_ShouldReturnFalseForHostVerified() {
        // Arrange
        hostProfile.setVerified(false);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(userDetailsService.getUserEntityByEmail("test@example.com")).thenReturn(userEntity);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-token");

        // Act
        AuthResponseDTO result = authService.login(loginRequest);

        // Assert
        assertFalse(result.isHostVerified());
    }

    @Test
    void register_WithValidData_ShouldReturnAuthResponse() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("newuser@example.com");
        when(userDetailsService.getUserEntityByEmail("newuser@example.com")).thenReturn(userEntity);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-token");

        // Act
        AuthResponseDTO result = authService.register(registerRequest);

        // Assert
        assertNotNull(result);
        assertEquals("jwt-token", result.token());
        assertEquals(1L, result.userId());
        assertEquals("test@example.com", result.email());

        verify(userService).register(registerRequest);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(userDetails);
    }

    @Test
    void getCurrentUser_WithAuthenticatedUser_ShouldReturnUserDTO() {
        // Arrange
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userDetailsService.getUserEntityByEmail("test@example.com")).thenReturn(userEntity);
        when(userMapper.toUserDTO(userEntity)).thenReturn(userDTO);

        // Act
        UserDTO result = authService.getCurrentUser();

        // Assert
        assertNotNull(result);
        assertEquals(userDTO, result);
        verify(userDetailsService).getUserEntityByEmail("test@example.com");
        verify(userMapper).toUserDTO(userEntity);
    }

    @Test
    void getCurrentUser_WithNoAuthentication_ShouldThrowUserNotFoundException() {
        // Arrange
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(null);

        // Act & Assert
        UserNotFoundException exception = assertThrows(
            UserNotFoundException.class,
            () -> authService.getCurrentUser()
        );

        assertEquals("No hay usuario autenticado", exception.getMessage());
        verifyNoInteractions(userDetailsService, userMapper);
    }

    @Test
    void getCurrentUser_WithUnauthenticatedUser_ShouldThrowUserNotFoundException() {
        // Arrange
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // Act & Assert
        UserNotFoundException exception = assertThrows(
            UserNotFoundException.class,
            () -> authService.getCurrentUser()
        );

        assertEquals("No hay usuario autenticado", exception.getMessage());
    }

    @Test
    void getCurrentUser_WithAnonymousUser_ShouldThrowUserNotFoundException() {
        // Arrange
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");

        // Act & Assert
        UserNotFoundException exception = assertThrows(
            UserNotFoundException.class,
            () -> authService.getCurrentUser()
        );

        assertEquals("No hay usuario autenticado", exception.getMessage());
    }

    @Test
    void logout_ShouldClearSecurityContext() {
        // Arrange
        SecurityContextHolder.setContext(securityContext);

        // Act
        authService.logout();

        // Assert
        // Verificamos que el contexto se haya limpiado
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void refreshToken_WithValidToken_ShouldReturnNewAuthResponse() {
        // Arrange
        String refreshToken = "valid-refresh-token";
        when(jwtService.extractUsername(refreshToken)).thenReturn("test@example.com");
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(userDetails);
        when(jwtService.isTokenValid(refreshToken, userDetails)).thenReturn(true);
        when(jwtService.generateToken(userDetails)).thenReturn("new-jwt-token");
        when(userDetailsService.getUserEntityByEmail("test@example.com")).thenReturn(userEntity);

        // Act
        AuthResponseDTO result = authService.refreshToken(refreshToken);

        // Assert
        assertNotNull(result);
        assertEquals("new-jwt-token", result.token());
        assertEquals(1L, result.userId());
        assertEquals("test@example.com", result.email());

        verify(jwtService).extractUsername(refreshToken);
        verify(jwtService).isTokenValid(refreshToken, userDetails);
        verify(jwtService).generateToken(userDetails);
    }

    @Test
    void refreshToken_WithInvalidToken_ShouldThrowInvalidPasswordException() {
        // Arrange
        String refreshToken = "invalid-token";
        when(jwtService.extractUsername(refreshToken)).thenReturn(null);

        // Act & Assert
        InvalidPasswordException exception = assertThrows(
            InvalidPasswordException.class,
            () -> authService.refreshToken(refreshToken)
        );

        assertEquals("Token de renovación inválido", exception.getMessage());
        verify(jwtService).extractUsername(refreshToken);
        verify(jwtService, never()).isTokenValid(anyString(), any());
    }

    @Test
    void refreshToken_WithExpiredToken_ShouldThrowInvalidPasswordException() {
        // Arrange
        String refreshToken = "expired-token";
        when(jwtService.extractUsername(refreshToken)).thenReturn("test@example.com");
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(userDetails);
        when(jwtService.isTokenValid(refreshToken, userDetails)).thenReturn(false);

        // Act & Assert
        InvalidPasswordException exception = assertThrows(
            InvalidPasswordException.class,
            () -> authService.refreshToken(refreshToken)
        );

        assertEquals("Token de renovación inválido", exception.getMessage());
        verify(jwtService).isTokenValid(refreshToken, userDetails);
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void refreshToken_WithException_ShouldThrowInvalidPasswordException() {
        // Arrange
        String refreshToken = "problematic-token";
        when(jwtService.extractUsername(refreshToken)).thenThrow(new RuntimeException("JWT parsing error"));

        // Act & Assert
        InvalidPasswordException exception = assertThrows(
            InvalidPasswordException.class,
            () -> authService.refreshToken(refreshToken)
        );

        assertEquals("Token de renovación inválido", exception.getMessage());
    }

    @Test
    void becomeHost_WithAuthenticatedUser_ShouldReturnUpdatedUserDTO() {
        // Arrange
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userDetailsService.getUserEntityByEmail("test@example.com")).thenReturn(userEntity);
        when(userMapper.toUserDTO(userEntity)).thenReturn(userDTO);

        UserDTO hostUserDTO = new UserDTO(
            1L,
            "Test User",
            "test@example.com",
            "1234567890",
            LocalDate.of(1990, 1, 1),
            Set.of(UserRole.USER, UserRole.HOST),
            null,
            null,
            null,
            true,
            true,
            null,
            null,
            false
        );
        when(userService.convertToHost(1L)).thenReturn(hostUserDTO);

        // Act
        UserDTO result = authService.becomeHost();

        // Assert
        assertNotNull(result);
        assertEquals(hostUserDTO, result);
        verify(userService).convertToHost(1L);
    }

    @Test
    void becomeHost_WithUnauthenticatedUser_ShouldThrowUserNotFoundException() {
        // Arrange
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(null);

        // Act & Assert
        UserNotFoundException exception = assertThrows(
            UserNotFoundException.class,
            () -> authService.becomeHost()
        );

        assertEquals("No hay usuario autenticado", exception.getMessage());
        verify(userService, never()).convertToHost(any());
    }
}