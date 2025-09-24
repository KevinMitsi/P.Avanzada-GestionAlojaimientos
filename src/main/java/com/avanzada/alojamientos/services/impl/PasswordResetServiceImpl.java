package com.avanzada.alojamientos.services.impl;

import com.avanzada.alojamientos.DTO.PasswordResetDto;
import com.avanzada.alojamientos.DTO.PasswordResetRequestDTO;
import com.avanzada.alojamientos.services.PasswordResetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PasswordResetServiceImpl implements PasswordResetService {
    @Override
    public void requestReset(PasswordResetRequestDTO dto) {

    }

    @Override
    public void resetPassword(PasswordResetDto dto) {

    }
}
