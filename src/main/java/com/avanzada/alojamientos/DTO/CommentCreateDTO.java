package com.avanzada.alojamientos.DTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record CommentCreateDTO(
        @NotNull @Min(1) @Max(5) Integer rating,
        @NotBlank @Length(max = 500) String text
) {
}
