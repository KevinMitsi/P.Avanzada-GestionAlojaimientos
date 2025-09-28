package com.avanzada.alojamientos.controllers;

import com.avanzada.alojamientos.DTO.user.CreateUserDTO;
import com.avanzada.alojamientos.DTO.user.EditUserDTO;
import com.avanzada.alojamientos.DTO.auth.RegisterUserDTO;
import com.avanzada.alojamientos.DTO.user.UserDTO;

import com.avanzada.alojamientos.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public UserDTO register(@RequestBody @Valid RegisterUserDTO dto) {
        return userService.register(dto);
    }

    @PostMapping
    public UserDTO create(@RequestBody @Valid CreateUserDTO dto) {
        return userService.create(dto);
    }

    @PutMapping("/{userId}")
    public UserDTO edit(@PathVariable Long userId, @RequestBody @Valid EditUserDTO dto) {
        return userService.edit(userId, dto);
    }

    @GetMapping("/{userId}")
    public Optional<UserDTO> findById(@PathVariable Long userId) {
        return userService.findById(userId);
    }

    @GetMapping
    public List<UserDTO> findAll() {
        return userService.findAll();
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
}
