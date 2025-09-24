package com.avanzada.alojamientos.DTO;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record ReplyCommentDTO(
        @NotBlank @Length(max = 500) String text
) {
}
