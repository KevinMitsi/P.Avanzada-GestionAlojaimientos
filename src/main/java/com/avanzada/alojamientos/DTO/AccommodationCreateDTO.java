package com.avanzada.alojamientos.DTO;

import com.avanzada.alojamientos.Model.Coordinates;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.util.List;

public record AccommodationCreateDTO(
        @NotBlank @Length(max = 200) String title,
        @Length(max = 1000) String description,
        @NotBlank String cityId,
        @Length(max = 300) String address,
        Coordinates coordinates,
        @NotNull @Positive BigDecimal pricePerNight,
        List<String> services,
        @NotNull @Min(1) Integer maxGuests
) {
}
