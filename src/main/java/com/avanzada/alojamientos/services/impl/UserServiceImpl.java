package com.avanzada.alojamientos.services.impl;

import com.avanzada.alojamientos.DTO.user.CreateUserDTO;
import com.avanzada.alojamientos.DTO.user.EditUserDTO;
import com.avanzada.alojamientos.DTO.auth.RegisterUserDTO;
import com.avanzada.alojamientos.DTO.user.UserDTO;

import com.avanzada.alojamientos.services.UserService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserServiceImpl implements UserService {


    @Override
    public UserDTO register(RegisterUserDTO dto) {
        return null;
    }

    @Override
    public UserDTO create(CreateUserDTO dto) {
        return null;
    }

    @Override
    public UserDTO edit(Long userId, EditUserDTO dto) {
        return null;
    }

    @Override
    public Optional<UserDTO> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public List<UserDTO> findAll() {
        return List.of();
    }

    @Override
    public void enable(String userId, boolean enable) {
        log.info("Not needed yet");
    }

    @Override
    public void delete(String userId) {
        log.info("Not implemented yet");
    }

    @Override
    public void changePassword(String userId, String oldPassword, String newPassword) {
        log.info("Not completed yet");
    }
}
