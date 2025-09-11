package com.avanzada.alojamientos.services.impl;

import com.avanzada.alojamientos.exceptions.UnauthorizedException;
import com.avanzada.alojamientos.exceptions.UserNotFoundException;
import com.avanzada.alojamientos.services.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Override
    public String getUsers(boolean isAdmin) {
        if (!isAdmin){
            throw  new UnauthorizedException("No tienes permisos para ver los usuarios");
        }
        return "Usuarios: []";
    }

    @Override
    public String createUser(String data) {
        return "Usuarios creados";
    }

    @Override
    public String updateUser(Integer id) {
        if (id <= 0){
            throw new UserNotFoundException("El id del usuario no es valido");
        }
        return "Usuario con id"+id+" actualizado";
    }

    @Override
    public String deleteUser(Integer id) {
        if (id <= 0){
            throw new UserNotFoundException("El id del usuario no es valido");
        }
        return "Usuario con id"+id+" eliminado";
    }

    @Override
    public String login(String username, String password) {
       if (username.isBlank() || password.isBlank()){
           throw new UserNotFoundException("Credenciales no validas");
       }
       return "Token de autenticacion: asjdasgduyvsdjasbdjavjsb";
    }
}
