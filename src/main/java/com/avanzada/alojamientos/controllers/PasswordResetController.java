package com.avanzada.alojamientos.controllers;

import com.avanzada.alojamientos.DTO.auth.PasswordResetDto;
import com.avanzada.alojamientos.DTO.auth.PasswordResetRequestDTO;
import com.avanzada.alojamientos.services.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/password-reset")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/request")
    public ResponseEntity<String> requestReset(@RequestBody @Valid PasswordResetRequestDTO dto) {
        passwordResetService.requestReset(dto);
        return ResponseEntity.ok("If the email exists, a recovery code has been sent.");
    }

    @PutMapping
    public ResponseEntity<String> resetPassword(@RequestBody @Valid PasswordResetDto dto) {
        passwordResetService.resetPassword(dto);
        return ResponseEntity.ok("Password has been successfully reset.");
    }
}

