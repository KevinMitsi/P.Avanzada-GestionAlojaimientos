package com.avanzada.alojamientos.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

public record SessionLoginDTO (
    @NotBlank @Email String email,
    @NotBlank String password
){
}
