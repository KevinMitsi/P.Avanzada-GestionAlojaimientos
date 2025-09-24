package com.avanzada.alojamientos.services;

import com.avanzada.alojamientos.DTO.PasswordResetDto;
import com.avanzada.alojamientos.DTO.PasswordResetRequestDTO;

public interface PasswordResetService {
    void requestReset(PasswordResetRequestDTO dto);
    void resetPassword(PasswordResetDto dto);
}
