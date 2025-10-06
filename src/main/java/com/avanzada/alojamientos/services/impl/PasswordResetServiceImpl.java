package com.avanzada.alojamientos.services.impl;

import com.avanzada.alojamientos.DTO.auth.PasswordResetDto;
import com.avanzada.alojamientos.DTO.auth.PasswordResetRequestDTO;
import com.avanzada.alojamientos.entities.PasswordResetTokenEntity;
import com.avanzada.alojamientos.entities.UserEntity;
import com.avanzada.alojamientos.exceptions.InvalidTokenException;
import com.avanzada.alojamientos.exceptions.UnauthorizedException;
import com.avanzada.alojamientos.exceptions.UserNotFoundException;
import com.avanzada.alojamientos.repositories.PasswordResetTokenRepository;
import com.avanzada.alojamientos.repositories.UserRepository;
import com.avanzada.alojamientos.security.CustomUserDetailsService;
import com.avanzada.alojamientos.security.JwtService;
import com.avanzada.alojamientos.services.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Base64;

@Service
@Slf4j
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    private static final long TOKEN_EXPIRATION_MINUTES = 15;

    @Override
    @Transactional
    public void requestReset(PasswordResetRequestDTO dto) {
        log.info(" Solicitud de recuperación para: {}", dto.email());

        // 1. Buscar usuario
        UserEntity user = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new UserNotFoundException("No existe un usuario con el correo: " + dto.email()));

        // 2. Validar si está habilitado
        if (Boolean.FALSE.equals(user.getEnabled())) {
            throw new UnauthorizedException("La cuenta está deshabilitada");
        }

        // 3. Invalidar tokens anteriores
        tokenRepository.findByUserAndUsedFalse(user).forEach(token -> {
            token.setUsed(true);
            tokenRepository.save(token);
        });

        // 4. Generar token JWT
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String jwtToken = jwtService.generateToken(userDetails);

        // 5. Guardar en BD
        PasswordResetTokenEntity tokenEntity = new PasswordResetTokenEntity();
        tokenEntity.setUser(user);
        tokenEntity.setTokenHash(jwtToken);
        tokenEntity.setExpiresAt(LocalDateTime.now().plusMinutes(TOKEN_EXPIRATION_MINUTES));
        tokenEntity.setUsed(false);
        tokenRepository.save(tokenEntity);

        log.info(" Token JWT generado para restablecimiento (expira en {} minutos)", TOKEN_EXPIRATION_MINUTES);

        // 6. Aquí va la lógica para enviar el correo con el token JWT
        // TODO: Implementar envío de correo con el token de recuperación
    }




    @Override
    @Transactional
    public void resetPassword(PasswordResetDto dto) {
        log.info("Intentando restablecer la contraseña...");

        String userEmail;

        // 1. Extraer el email del token JWT
        try {
            userEmail = jwtService.extractUsername(dto.token());
        } catch (Exception e) {
            throw new InvalidTokenException("El token de recuperación es inválido o está corrupto");
        }

        // 2. Buscar usuario asociado
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con el token proporcionado"));

        // 3. Buscar el token activo en la BD
        PasswordResetTokenEntity tokenEntity = tokenRepository.findByTokenHashAndUsedFalse(dto.token())
                .orElseThrow(() -> new InvalidTokenException("El token de recuperación ya fue usado o no existe"));

        // 4. Validar expiración
        if (tokenEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("El token de recuperación ha expirado. Solicita uno nuevo.");
        }

        // 5. Actualizar contraseña
        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // 6. Marcar token como usado
        tokenEntity.setUsed(true);
        tokenRepository.save(tokenEntity);

        log.info(" Contraseña restablecida correctamente para {}", user.getEmail());

        // 7. Aquí va la lógica para enviar el correo de confirmación
        // TODO: Implementar envío de correo de confirmación de restablecimiento
    }

}
