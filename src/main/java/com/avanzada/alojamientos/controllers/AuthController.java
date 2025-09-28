package com.avanzada.alojamientos.controllers;

import com.avanzada.alojamientos.DTO.auth.SessionLoginDTO;
import com.avanzada.alojamientos.DTO.user.UserDTO;
import com.avanzada.alojamientos.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public UserDTO login(@RequestBody @Valid SessionLoginDTO dto) {
        return authService.login(dto);
    }

    @PostMapping("/logout/{userId}")
    public void logout(@PathVariable Long userId) {
        authService.logout(userId);
    }
}
