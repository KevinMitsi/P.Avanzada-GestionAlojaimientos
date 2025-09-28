package com.avanzada.alojamientos.services;

import com.avanzada.alojamientos.DTO.auth.PasswordResetDto;
import com.avanzada.alojamientos.DTO.auth.PasswordResetRequestDTO;

public interface PasswordResetService {
    void requestReset(PasswordResetRequestDTO dto);
    void resetPassword(PasswordResetDto dto);
}
