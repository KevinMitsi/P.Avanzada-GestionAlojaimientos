package com.avanzada.alojamientos.controllers;

import com.avanzada.alojamientos.DTO.auth.PasswordResetDto;
import com.avanzada.alojamientos.DTO.auth.PasswordResetRequestDTO;
import com.avanzada.alojamientos.services.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/password-reset")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/request")
    public void requestReset(@RequestBody @Valid PasswordResetRequestDTO dto) {
        passwordResetService.requestReset(dto);
    }

    @PostMapping
    public void resetPassword(@RequestBody @Valid PasswordResetDto dto) {
        passwordResetService.resetPassword(dto);
    }
}

