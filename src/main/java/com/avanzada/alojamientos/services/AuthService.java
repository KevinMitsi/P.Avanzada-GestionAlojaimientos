package com.avanzada.alojamientos.services;

import com.avanzada.alojamientos.DTO.SessionLoginDTO;
import com.avanzada.alojamientos.DTO.UserDTO;

public interface AuthService {
    UserDTO login(SessionLoginDTO dto);
    void logout(Long userId);
}
