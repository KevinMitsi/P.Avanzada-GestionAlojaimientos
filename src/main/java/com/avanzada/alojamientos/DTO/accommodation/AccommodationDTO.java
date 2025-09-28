package com.avanzada.alojamientos.DTO.accommodation;

import com.avanzada.alojamientos.DTO.city.CityDTO;
import com.avanzada.alojamientos.DTO.other.CoordinatesDTO;
import com.avanzada.alojamientos.DTO.other.ImageDTO;

import java.math.BigDecimal;
import java.util.List;

public record AccommodationDTO(
        Long id,
        Long hostId,
        String title,
        String description,
        CityDTO city,
        String address,
        CoordinatesDTO coordinates,
        BigDecimal pricePerNight,
        List<String> services,
        List<ImageDTO> images,
        Integer maxGuests,
        Boolean active,
        Boolean softDeleted,
        String deletedAt,
        String createdAt,
        String updatedAt,
        Integer countReservations,
        Double avgRating
) {
}
