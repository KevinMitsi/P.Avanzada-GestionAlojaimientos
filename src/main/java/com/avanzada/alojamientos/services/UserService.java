package com.avanzada.alojamientos.services;

import com.avanzada.alojamientos.DTO.CreateUserDTO;
import com.avanzada.alojamientos.DTO.EditUserDTO;
import com.avanzada.alojamientos.DTO.RegisterUserDTO;
import com.avanzada.alojamientos.DTO.UserDTO;


import java.util.List;
import java.util.Optional;

public interface UserService {
    UserDTO register(RegisterUserDTO dto);
    UserDTO create(CreateUserDTO dto);
    UserDTO edit(Long userId, EditUserDTO dto);
    Optional<UserDTO> findById(Long id);
    List<UserDTO> findAll();
    void enable(String userId, boolean enable);
    void delete(String userId);
    void changePassword(String userId, String oldPassword, String newPassword);
}
