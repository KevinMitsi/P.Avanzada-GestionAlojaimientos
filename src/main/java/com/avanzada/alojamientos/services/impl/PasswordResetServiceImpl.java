package com.avanzada.alojamientos.services.impl;

import com.avanzada.alojamientos.DTO.auth.PasswordResetDto;
import com.avanzada.alojamientos.DTO.auth.PasswordResetRequestDTO;
import com.avanzada.alojamientos.DTO.notification.EmailDTO;
import com.avanzada.alojamientos.entities.PasswordResetTokenEntity;
import com.avanzada.alojamientos.entities.UserEntity;
import com.avanzada.alojamientos.exceptions.InvalidTokenException;
import com.avanzada.alojamientos.exceptions.RecoveryTokenException;
import com.avanzada.alojamientos.exceptions.UnauthorizedException;
import com.avanzada.alojamientos.exceptions.UserNotFoundException;
import com.avanzada.alojamientos.repositories.PasswordResetTokenRepository;
import com.avanzada.alojamientos.repositories.UserRepository;
import com.avanzada.alojamientos.security.CustomUserDetailsService;
import com.avanzada.alojamientos.security.JwtService;
import com.avanzada.alojamientos.services.EmailNotificationService;
import com.avanzada.alojamientos.services.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@Slf4j
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailNotificationService notificationService;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;

    private static final int TOKEN_EXPIRATION_MINUTES = 15;

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

        log.info("Password reset token created for user ID: {} - Token expires at: {}",
                user.getId(), tokenEntity.getExpiresAt());

        String recoveryCode = generateRecoveryCode();

        // 7. Enviar email con el código de recuperación
        sendPasswordResetEmail(user, recoveryCode);

        log.info("Password reset email sent successfully to: {}", dto.email());
    }

    @Override
    @Transactional
    public void resetPassword(PasswordResetDto dto) {
        log.info("Attempting password reset with provided token");

        String userEmail = jwtService.extractUsername(dto.token());

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

        log.info("Password successfully reset for user ID: {}", user.getId());

        // 7. Enviar email de confirmación
        sendPasswordChangeConfirmationEmail(user);

        log.info("Password change confirmation email sent successfully to: {}", user.getEmail());
    }

    private void invalidatePreviousTokens(UserEntity user) {
        tokenRepository.findByUserAndUsedFalse(user)
                .forEach(token -> {
                    token.setUsed(true);
                    tokenRepository.save(token);
                    log.debug("Invalidated previous token for user ID: {}", user.getId());
                });
    }

    private String generateRecoveryCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000); // Genera número entre 100000 y 999999
        return String.valueOf(code);
    }

    /**
     * Crea un hash SHA-256 del token para almacenarlo de forma segura
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("Error hashing token", e);
            throw new RecoveryTokenException("Error processing recovery token");
        }
    }

    /**
     * Envía el email con el código de recuperación de contraseña
     */
    private void sendPasswordResetEmail(UserEntity user, String recoveryCode) {
        try {
            String subject = "Recuperación de Contraseña - StayGo";
            String body = String.format(
                """
                Hola %s,
                
                Has solicitado restablecer tu contraseña en StayGo.
                
                Tu código de recuperación es: %s
                
                Este código expira en %d minutos.
                
                Si no solicitaste este cambio, puedes ignorar este correo.
                
                Saludos,
                Equipo de Alojamientos
                """,
                user.getName() != null ? user.getName() : "Usuario",
                recoveryCode,
                TOKEN_EXPIRATION_MINUTES
            );

            EmailDTO emailDTO = new EmailDTO(subject, body, user.getEmail());
            notificationService.sendMail(emailDTO);

            log.info("Password reset email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Error sending password reset email to: {}", user.getEmail(), e);
            }
    }

    /**
     * Envía el email de confirmación de cambio de contraseña
     */
    private void sendPasswordChangeConfirmationEmail(UserEntity user) {
        try {
            String subject = "Contraseña Cambiada Exitosamente - StayGo";
            String body = String.format(
                """
                Hola %s,
                
                Tu contraseña ha sido cambiada exitosamente en StayGo.
                
                Fecha y hora del cambio: %s
                
                Si no realizaste este cambio, contacta inmediatamente con nuestro soporte.
                
                Saludos,
                Equipo de Alojamientos
                """,
                user.getName() != null ? user.getName() : "Usuario",
                LocalDateTime.now()
            );

            EmailDTO emailDTO = new EmailDTO(subject, body, user.getEmail());
            notificationService.sendMail(emailDTO);

            log.info("Password change confirmation email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Error sending password change confirmation email to: {}", user.getEmail(), e);
        }
    }
}
