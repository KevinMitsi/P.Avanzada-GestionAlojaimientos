package com.avanzada.alojamientos.services;

public interface UserService {
    String getUsers(boolean isAdmin);
    String createUser(String data);
    String updateUser(Integer id);
    String deleteUser(Integer id);
    String login(String username, String password);
}
