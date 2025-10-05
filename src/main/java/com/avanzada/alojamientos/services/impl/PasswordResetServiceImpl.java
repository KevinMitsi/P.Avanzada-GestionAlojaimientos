package com.avanzada.alojamientos.services.impl;

import com.avanzada.alojamientos.DTO.auth.PasswordResetDto;
import com.avanzada.alojamientos.DTO.auth.PasswordResetRequestDTO;
import com.avanzada.alojamientos.entities.PasswordResetTokenEntity;
import com.avanzada.alojamientos.entities.UserEntity;
import com.avanzada.alojamientos.exceptions.UnauthorizedException;
import com.avanzada.alojamientos.exceptions.UserNotFoundException;
import com.avanzada.alojamientos.repositories.PasswordResetTokenRepository;
import com.avanzada.alojamientos.repositories.UserRepository;
import com.avanzada.alojamientos.services.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private static final int TOKEN_LENGTH = 6;
    private static final int TOKEN_EXPIRATION_MINUTES = 15;

    @Override
    @Transactional
    public void requestReset(PasswordResetRequestDTO dto) {
        log.info("Password reset requested for email: {}", dto.email());

        // 1. Buscar usuario por email
        UserEntity user = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new UserNotFoundException("There is no user with the email: " + dto.email()));

        // 2. Validar que el usuario esté habilitado
        if (Boolean.FALSE.equals(user.getEnabled())) {
            throw new UnauthorizedException("The account is disabled");
        }

        // 3. Invalidar tokens anteriores del usuario (marcarlos como usados)
        invalidatePreviousTokens(user);

        // 4. Generar código de recuperación (6 dígitos)
        String recoveryCode = generateRecoveryCode();

        // 5. Crear hash del código para almacenar en BD
        String tokenHash = hashToken(recoveryCode);

        // 6. Crear entidad del token
        PasswordResetTokenEntity tokenEntity = new PasswordResetTokenEntity();
        tokenEntity.setUser(user);
        tokenEntity.setTokenHash(tokenHash);
        tokenEntity.setExpiresAt(LocalDateTime.now().plusMinutes(TOKEN_EXPIRATION_MINUTES));
        tokenEntity.setUsed(false);
        tokenEntity.setCreatedAt(LocalDateTime.now());

        tokenRepository.save(tokenEntity);

        log.info("Password reset token created for user ID: {} - Token expires at: {}",
                user.getId(), tokenEntity.getExpiresAt());

        // TODO: Enviar email con el código de recuperación


        // TEMPORAL: Imprimir código en consola para pruebas
        log.warn("⚠️ CÓDIGO DE RECUPERACIÓN (solo para desarrollo): {}", recoveryCode);
        log.warn("⚠️ Este código expira en {} minutos", TOKEN_EXPIRATION_MINUTES);
    }




    @Override
    @Transactional
    public void resetPassword(PasswordResetDto dto) {

        log.info("Attempting password reset with provided token");

        // 1. Crear hash del token recibido
        String tokenHash = hashToken(dto.token());

        // 2. Buscar token válido en la BD
        PasswordResetTokenEntity tokenEntity = tokenRepository
                .findByTokenHashAndUsedFalse(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("Invalid recovery code"));

        // 3. Validar que el token no haya expirado
        if (tokenEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("The recovery code has expired. Request a new one.");
        }

        // 4. Obtener usuario asociado al token
        UserEntity user = tokenEntity.getUser();

        // 5. Actualizar contraseña del usuario
        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // 6. Marcar el token como usado
        tokenEntity.setUsed(true);
        tokenRepository.save(tokenEntity);

        log.info("Password successfully reset for user ID: {}", user.getId());

        // TODO: Enviar email de confirmación





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
            throw new RuntimeException("Error processing recovery token");
        }
    }

}
