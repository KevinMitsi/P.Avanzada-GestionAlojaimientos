package com.avanzada.alojamientos.DTO.other;

import com.avanzada.alojamientos.DTO.city.CityDTO;

import java.math.BigDecimal;
import java.util.List;

public record FavoriteAccommodationDTO(
        Long id,
        String title,
        CityDTO city,
        String address,
        BigDecimal pricePerNight,
        Integer maxGuests,
        Double avgRating,
        List<ImageDTO> images
) {
}
