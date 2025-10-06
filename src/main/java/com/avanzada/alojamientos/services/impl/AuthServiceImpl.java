package com.avanzada.alojamientos.services.impl;

import com.avanzada.alojamientos.DTO.auth.AuthResponseDTO;
import com.avanzada.alojamientos.DTO.auth.LoginRequestDTO;
import com.avanzada.alojamientos.DTO.auth.RegisterUserDTO;
import com.avanzada.alojamientos.DTO.user.UserDTO;
import com.avanzada.alojamientos.entities.UserEntity;
import com.avanzada.alojamientos.exceptions.InvalidPasswordException;
import com.avanzada.alojamientos.exceptions.UserNotFoundException;
import com.avanzada.alojamientos.mappers.UserMapper;
import com.avanzada.alojamientos.security.CustomUserDetailsService;
import com.avanzada.alojamientos.security.JwtService;
import com.avanzada.alojamientos.services.AuthService;
import com.avanzada.alojamientos.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final CustomUserDetailsService userDetailsService;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public AuthResponseDTO login(LoginRequestDTO loginRequest) {
        try {
            log.info("Intentando autenticar usuario: {}", loginRequest.email());

            // Autenticar usando Spring Security
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password())
            );

            // Obtener detalles del usuario
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            UserEntity user = userDetailsService.getUserEntityByEmail(userDetails.getUsername());

            // Generar token JWT (único token soportado por JwtService actual)
            String token = jwtService.generateToken(userDetails);

            log.info("Usuario autenticado exitosamente: {}", loginRequest.email());

            // Construir respuesta según AuthResponseDTO disponible
            return new AuthResponseDTO(
                token,
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRoles(),
                Boolean.TRUE.equals(user.getVerified()),
                (user.getHostProfile() != null) && Boolean.TRUE.equals(user.getHostProfile().getVerified())
            );

        } catch (BadCredentialsException e) {
            log.warn("Credenciales inválidas para: {}", loginRequest.email());
            throw new InvalidPasswordException("Credenciales inválidas");
        }
    }

    @Override
    @Transactional
    public AuthResponseDTO register(RegisterUserDTO registerRequest) {
        log.info("Registrando nuevo usuario: {}", registerRequest.email());
        userService.register(registerRequest);

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(registerRequest.email(), registerRequest.password())
        );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        UserEntity user = userDetailsService.getUserEntityByEmail(userDetails.getUsername());

        String token = jwtService.generateToken(userDetails);
        log.info("Usuario registrado y autenticado: {}", registerRequest.email());

        return new AuthResponseDTO(
            token,
            user.getId(),
            user.getEmail(),
            user.getName(),
            user.getRoles(),
            Boolean.TRUE.equals(user.getVerified()),
            (user.getHostProfile() != null) && Boolean.TRUE.equals(user.getHostProfile().getVerified())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
            authentication.getPrincipal().equals("anonymousUser")) {
            throw new UserNotFoundException("No hay usuario autenticado");
        }

        String email = authentication.getName();
        UserEntity user = userDetailsService.getUserEntityByEmail(email);

        return userMapper.toUserDTO(user);
    }

    @Override
    public void logout() {
        SecurityContextHolder.clearContext();
        log.info("Usuario deslogueado exitosamente");
    }

    @Override
    public AuthResponseDTO refreshToken(String refreshToken) {
        try {
            log.info("Intentando renovar token");

            String email = jwtService.extractUsername(refreshToken);
            if (email == null) {
                throw new InvalidPasswordException("Token inválido");
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            if (!jwtService.isTokenValid(refreshToken, userDetails)) {
                throw new InvalidPasswordException("Token inválido");
            }

            String newToken = jwtService.generateToken(userDetails);
            UserEntity user = userDetailsService.getUserEntityByEmail(email);

            log.info("Token renovado exitosamente para: {}", email);

            return new AuthResponseDTO(
                newToken,
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRoles(),
                Boolean.TRUE.equals(user.getVerified()),
                (user.getHostProfile() != null) && Boolean.TRUE.equals(user.getHostProfile().getVerified())
            );
        } catch (Exception e) {
            log.error("Error renovando token: {}", e.getMessage());
            throw new InvalidPasswordException("Token de renovación inválido");
        }
    }

    @Override
    @Transactional
    public UserDTO becomeHost() {
        log.info("Usuario actual solicitando convertirse en HOST");

        // Obtener el usuario actual autenticado
        UserDTO currentUser = getCurrentUser();

        // Convertir a HOST usando UserService
        UserDTO updatedUser = userService.convertToHost(currentUser.id());

        log.info("Usuario {} convertido exitosamente a HOST", currentUser.email());

        return updatedUser;
    }
}
