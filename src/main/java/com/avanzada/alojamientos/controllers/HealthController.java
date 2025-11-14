package com.avanzada.alojamientos.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/")
    public String home() {
        return "Alojamientos API is running!";
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}