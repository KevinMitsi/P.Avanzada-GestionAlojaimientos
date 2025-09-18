package com.avanzada.alojamientos.controllers;

import com.avanzada.alojamientos.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

//    private final UserService userService;
//
//    @PostMapping("/login")
//    public ResponseEntity<String> login(String username, String password) {
//        String token = userService.login(username, password);
//        return ResponseEntity.ok(token);
//    }
//
//    @GetMapping("/users")
//    public ResponseEntity<String> getUsers() {
//        boolean isAdmin = true; // Simulamos que el usuario es admin
//        String data = userService.getUsers(isAdmin);
//        return ResponseEntity.ok(data);
//    }
//
//    @PostMapping("/user")
//    public ResponseEntity<String> registerUser(String data) {
//        String response = userService.createUser(data);
//        return ResponseEntity.ok(response);
//    }
//
//    @PutMapping("/user/{id}")
//    public ResponseEntity<String> updateUser(@PathVariable Integer id) {
//        String response = userService.updateUser(id);
//        return ResponseEntity.ok(response);
//    }
//
//    @DeleteMapping("/user/{id}")
//    public ResponseEntity<String> deleteUser(@PathVariable Integer id) {
//        String response = userService.deleteUser(id);
//        return ResponseEntity.ok(response);
//    }
}
