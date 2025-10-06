package com.avanzada.alojamientos.controllers;

import com.avanzada.alojamientos.DTO.auth.AuthResponseDTO;
import com.avanzada.alojamientos.DTO.auth.LoginRequestDTO;
import com.avanzada.alojamientos.DTO.auth.RegisterUserDTO;
import com.avanzada.alojamientos.DTO.user.UserDTO;
import com.avanzada.alojamientos.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * Login de usuario
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        log.info("Petición de login recibida para: {}", loginRequest.email());
        AuthResponseDTO response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Registro de nuevo usuario
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterUserDTO registerRequest) {
        log.info("Petición de registro recibida para: {}", registerRequest.email());
        AuthResponseDTO response = authService.register(registerRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtener información del usuario actual autenticado
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> getCurrentUser() {
        log.debug("Obteniendo información del usuario actual");
        UserDTO user = authService.getCurrentUser();
        return ResponseEntity.ok(user);
    }

    /**
     * Logout del usuario
     */
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> logout() {
        log.info("Petición de logout recibida");
        authService.logout();
        return ResponseEntity.ok("Logout exitoso");
    }

    /**
     * Renovar token JWT
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refreshToken(@RequestParam String refreshToken) {
        log.info("Petición de refresh token recibida");
        AuthResponseDTO response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }

    /**
     * Convertirse en HOST - cualquier usuario autenticado puede hacerlo
     */
    @PutMapping("/become-host")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> becomeHost() {
        log.info("Usuario solicitando convertirse en HOST");
        UserDTO updatedUser = authService.becomeHost();
        return ResponseEntity.ok(updatedUser);
    }
}
