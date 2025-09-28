package com.avanzada.alojamientos.DTO.accommodation;


import com.avanzada.alojamientos.DTO.other.CoordinatesDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.util.List;

public record UpdateAccommodationDTO(
        @NotBlank @Length(min = 5, max = 200) String title,
        @NotBlank @Length(max = 1000) String description,
        @Length(max = 300) String address,
        @Valid CoordinatesDTO coordinates,
        @NotNull @Positive BigDecimal pricePerNight,
        List<String> services,
        @NotNull @Min(1) Integer maxGuests,
        Boolean active
) {
}
