package com.avanzada.alojamientos.services.impl;

import com.avanzada.alojamientos.DTO.auth.PasswordResetDto;
import com.avanzada.alojamientos.DTO.auth.PasswordResetRequestDTO;
import com.avanzada.alojamientos.Security.JWTUtils;
import com.avanzada.alojamientos.entities.PasswordResetTokenEntity;
import com.avanzada.alojamientos.entities.UserEntity;
import com.avanzada.alojamientos.exceptions.InvalidTokenException;
import com.avanzada.alojamientos.exceptions.TokenExpiredException;
import com.avanzada.alojamientos.exceptions.UserNotFoundException;
import com.avanzada.alojamientos.repositories.PasswordResetTokenRepository;
import com.avanzada.alojamientos.repositories.UserRepository;
import com.avanzada.alojamientos.services.PasswordResetService;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtils jwtUtils;

    @Transactional
    @Override
    public void requestReset(PasswordResetRequestDTO dto) {
        // Buscar usuario por email
        UserEntity user = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new UserNotFoundException("No existe un usuario con ese correo."));

        // Invalidar tokens anteriores
        List<PasswordResetTokenEntity> oldTokens = tokenRepository.findByUserAndUsedFalse(user);
        oldTokens.forEach(t -> t.setUsed(true));
        tokenRepository.saveAll(oldTokens);

        // Generar nuevo JWT con expiración de 15 minutos
        String jwtToken = jwtUtils.generateToken(
                user.getId().toString(),
                Map.of("email", user.getEmail(), "type", "password-reset")
        );

        // Guardar token sin encriptar
        PasswordResetTokenEntity tokenEntity = new PasswordResetTokenEntity();
        tokenEntity.setUser(user);
        tokenEntity.setTokenHash(jwtToken);
        tokenEntity.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        tokenEntity.setUsed(false);
        tokenRepository.save(tokenEntity);

        // TODO: Enviar el correo aquí


        log.info("Token de recuperación generado para {} (expira en 15 minutos)", user.getEmail());
    }

    @Transactional
    @Override
    public void resetPassword(PasswordResetDto dto) {
        String jwtToken = dto.token();

        // Validar el JWT (expiración, integridad, firma)
        try {
            jwtUtils.parseJwt(jwtToken);
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException("El token ha expirado. Solicite uno nuevo.");
        } catch (Exception e) {
            throw new InvalidTokenException("El token de recuperación es inválido.");
        }

        // Buscar token en la base de datos
        PasswordResetTokenEntity tokenEntity = tokenRepository.findByTokenHashAndUsedFalse(jwtToken)
                .orElseThrow(() -> new InvalidTokenException("El token no es válido o ya fue utilizado."));

        // Validar expiración
        if (tokenEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException("El token ha expirado. Solicite uno nuevo.");
        }

        // Actualizar la contraseña del usuario
        UserEntity user = tokenEntity.getUser();
        if (user == null) {
            throw new UserNotFoundException("El usuario asociado al token no existe.");
        }

        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        userRepository.save(user);

        // Marcar el token como usado
        tokenEntity.setUsed(true);
        tokenRepository.save(tokenEntity);

        log.info("Contraseña restablecida correctamente para usuario {}", user.getEmail());
    }
}


