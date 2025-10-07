package com.avanzada.alojamientos.DTO.accommodation;

import com.avanzada.alojamientos.DTO.city.CityDTO;
import com.avanzada.alojamientos.DTO.other.CoordinatesDTO;

import java.math.BigDecimal;
import java.util.List;

public record CreateAccommodationResponseDTO(
        Long id,
        Long hostId,
        String title,
        String description,
        CityDTO city,
        String address,
        CoordinatesDTO coordinates,
        BigDecimal pricePerNight,
        List<String> services,
        Integer maxGuests,
        String createdAt
) {
}
