package com.avanzada.alojamientos.DTO;

import com.avanzada.alojamientos.DTO.model.ReservationStatus;

public record ReservationSearchCriteria(
        String userId,
        String hostId,
        String accommodationId,
        ReservationStatus status,
        DateRange dateRange
) {}
