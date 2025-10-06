package com.avanzada.alojamientos.services;

import com.avanzada.alojamientos.DTO.auth.AuthResponseDTO;
import com.avanzada.alojamientos.DTO.auth.LoginRequestDTO;
import com.avanzada.alojamientos.DTO.auth.RegisterUserDTO;
import com.avanzada.alojamientos.DTO.user.UserDTO;

public interface AuthService {
    AuthResponseDTO login(LoginRequestDTO loginRequest);
    AuthResponseDTO register(RegisterUserDTO registerRequest);
    UserDTO getCurrentUser();
    void logout();
    AuthResponseDTO refreshToken(String refreshToken);
    UserDTO becomeHost(); // Permitir que usuario actual se convierta en HOST
}
