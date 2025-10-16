package com.avanzada.alojamientos.DTO.accommodation;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "DTO simplificado para resultados de búsqueda de alojamientos")
public record AccommodationFoundDTO(
        @Schema(description = "ID del alojamiento", example = "1")
        Long id,

        @Schema(description = "Título del alojamiento", example = "Apartamento en el centro")
        String title,

        @Schema(description = "URL de la imagen principal del alojamiento", example = "https://res.cloudinary.com/.../image.jpg")
        String primaryImageUrl,

        @Schema(description = "Precio por noche", example = "150.00")
        BigDecimal pricePerNight,

        @Schema(description = "Rating promedio del alojamiento", example = "4.5")
        Double avgRating
) {
}
