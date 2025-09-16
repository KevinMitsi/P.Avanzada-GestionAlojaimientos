package com.avanzada.alojamientos.DTO;

import com.avanzada.alojamientos.Model.City;
import com.avanzada.alojamientos.Model.Coordinates;
import com.avanzada.alojamientos.Model.Imagen;

import java.math.BigDecimal;
import java.util.List;

public record AccommodationDTO(
        String id,
        String hostId,
        String title,
        String description,
        City city,
        String address,
        Coordinates coordinates,
        BigDecimal pricePerNight,
        List<String> services,
        List<Imagen> images,
        Integer maxGuests,
        String estado,
        Boolean active,
        Boolean softDeleted,
        String deletedAt,
        String createdAt,
        String updatedAt,
        Integer countReservations,
        Double avgRating
) {
}
