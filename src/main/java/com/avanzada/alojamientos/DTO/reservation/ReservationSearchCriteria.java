package com.avanzada.alojamientos.DTO.reservation;

import com.avanzada.alojamientos.DTO.other.DateRange;
import com.avanzada.alojamientos.DTO.model.ReservationStatus;

public record ReservationSearchCriteria(
        Long userId,
        Long hostId,
        Long accommodationId,
        ReservationStatus status,
        DateRange dateRange
) {}
