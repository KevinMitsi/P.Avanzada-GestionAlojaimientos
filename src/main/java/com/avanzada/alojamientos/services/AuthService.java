package com.avanzada.alojamientos.services;

import com.avanzada.alojamientos.DTO.auth.SessionLoginDTO;
import com.avanzada.alojamientos.DTO.user.UserDTO;

public interface AuthService {
    UserDTO login(SessionLoginDTO dto);
    void logout(Long userId);
}
