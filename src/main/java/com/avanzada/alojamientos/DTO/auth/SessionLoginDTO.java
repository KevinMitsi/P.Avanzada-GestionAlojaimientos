package com.avanzada.alojamientos.DTO.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

public record SessionLoginDTO (
    @NotBlank @Email String email,
    @NotBlank String password
){
}
