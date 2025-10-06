package com.avanzada.alojamientos.controllers;

import com.avanzada.alojamientos.DTO.user.EditUserDTO;
import com.avanzada.alojamientos.DTO.user.UserDTO;

import com.avanzada.alojamientos.security.CurrentUserService;
import com.avanzada.alojamientos.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CurrentUserService currentUserService;

    // Operaciones self-service sin enviar IDs
    @PutMapping("/me")
    public UserDTO editMe(@RequestBody @Valid EditUserDTO dto) {
        Long userId = currentUserService.getCurrentUserId();
        return userService.edit(userId, dto);
    }

    @GetMapping("/me")
    public Optional<UserDTO> findMe() {
        Long userId = currentUserService.getCurrentUserId();
        return userService.findById(userId);
    }

    @DeleteMapping("/me")
    public void deleteMe() {
        String userId = String.valueOf(currentUserService.getCurrentUserId());
        userService.delete(userId);
    }

    @PutMapping("/me/password")
    public void changeMyPassword(@RequestParam String oldPassword,
                               @RequestParam String newPassword) {
        String userId = String.valueOf(currentUserService.getCurrentUserId());
        userService.changePassword(userId, oldPassword, newPassword);
    }

    // Endpoints por ID (pensados para administración)
    @PutMapping("/{userId}")
    public UserDTO edit(@PathVariable Long userId, @RequestBody @Valid EditUserDTO dto) {
        return userService.edit(userId, dto);
    }

    @GetMapping("/{userId}")
    public Optional<UserDTO> findById(@PathVariable Long userId) {
        return userService.findById(userId);
    }

    @PutMapping("/{userId}/enable")
   public void enable(@PathVariable String userId, @RequestParam boolean enable) {
        userService.enable(userId, enable);
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable String userId) {
        userService.delete(userId);
    }

    @PutMapping("/{userId}/password")
    public void changePassword(@PathVariable String userId,
                               @RequestParam String oldPassword,
                               @RequestParam String newPassword) {
        userService.changePassword(userId, oldPassword, newPassword);
    }

    /**
     * Endpoint para convertir un usuario específico en HOST (solo para administradores)
     */
    @PostMapping("/{userId}/convert-to-host")
    public ResponseEntity<UserDTO> convertUserToHost(@PathVariable Long userId) {
        UserDTO updatedUser = userService.convertToHost(userId);
        return ResponseEntity.ok(updatedUser);
    }
}
