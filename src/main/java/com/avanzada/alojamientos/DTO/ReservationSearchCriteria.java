package com.avanzada.alojamientos.DTO;

import com.avanzada.alojamientos.DTO.model.ReservationStatus;

public record ReservationSearchCriteria(
        long userId,
        long hostId,
        long accommodationId,
        ReservationStatus status,
        DateRange dateRange
) {}
