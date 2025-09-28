package com.avanzada.alojamientos.services.impl;

import com.avanzada.alojamientos.DTO.auth.SessionLoginDTO;
import com.avanzada.alojamientos.DTO.user.UserDTO;
import com.avanzada.alojamientos.services.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {
    @Override
    public UserDTO login(SessionLoginDTO dto) {
        return null;
    }

    @Override
    public void logout(Long userId) {
        log.info("User with ID: {} logged out", userId);
    }
}
