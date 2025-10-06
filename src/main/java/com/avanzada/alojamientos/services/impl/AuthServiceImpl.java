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
        log.info("Intentando autenticar usuario: {}", loginRequest.email());

        try {
            // Autenticar con Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.email(),
                            loginRequest.password()
                    )
            );

            // Obtener detalles del usuario autenticado
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            UserEntity user = userDetailsService.getUserEntityByEmail(userDetails.getUsername());

            // Generar token JWT
            String token = jwtService.generateToken(userDetails);

            // Verificar si es host verificado
            boolean isHostVerified = userService.isVerifiedHost(user.getId());

            log.info("Usuario autenticado exitosamente: {}", loginRequest.email());

            return new AuthResponseDTO(
                    token,
                    user.getId(),
                    user.getEmail(),
                    user.getName(),
                    user.getRole(),
                    user.getVerified(),
                    isHostVerified
            );

        } catch (BadCredentialsException e) {
            log.warn("Intento de login fallido para: {}", loginRequest.email());
            throw new InvalidPasswordException("Credenciales inválidas");
        }
    }

    @Override
    @Transactional
    public AuthResponseDTO register(RegisterUserDTO registerRequest) {
        log.info("Registrando nuevo usuario: {}", registerRequest.email());

        // Registrar usuario usando el servicio existente
        UserDTO userDTO = userService.register(registerRequest);

        // Autenticar inmediatamente después del registro
        UserDetails userDetails = userDetailsService.loadUserByUsername(registerRequest.email());
        String token = jwtService.generateToken(userDetails);

        // Verificar si es host verificado (será false para nuevos registros)
        boolean isHostVerified = userService.isVerifiedHost(userDTO.id());

        log.info("Usuario registrado y autenticado exitosamente: {}", registerRequest.email());

        return new AuthResponseDTO(
                token,
                userDTO.id(),
                userDTO.email(),
                userDTO.name(),
                userDTO.role(),
                userDTO.verified(),
                isHostVerified
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
        // En una implementación con JWT stateless, el logout se maneja en el cliente
        // eliminando el token. Aquí podemos limpiar el contexto de seguridad
        SecurityContextHolder.clearContext();
        log.info("Usuario deslogueado exitosamente");
    }

    @Override
    public AuthResponseDTO refreshToken(String refreshToken) {
        // Implementación de refresh token (opcional para esta versión)
        // Por ahora, solo validamos el token existente
        try {
            String email = jwtService.extractUsername(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if (jwtService.isTokenValid(refreshToken, userDetails)) {
                UserEntity user = userDetailsService.getUserEntityByEmail(email);
                String newToken = jwtService.generateToken(userDetails);
                boolean isHostVerified = userService.isVerifiedHost(user.getId());

                return new AuthResponseDTO(
                        newToken,
                        user.getId(),
                        user.getEmail(),
                        user.getName(),
                        user.getRole(),
                        user.getVerified(),
                        isHostVerified
                );
            } else {
                throw new InvalidPasswordException("Token inválido o expirado");
            }
        } catch (Exception e) {
            log.error("Error al refrescar token: {}", e.getMessage());
            throw new InvalidPasswordException("Token inválido");
        }
    }

    @Override
    @Transactional
    public UserDTO becomeHost() {
        log.info("Usuario actual solicitando convertirse en HOST");
        return userService.becomeHost();
    }
}
