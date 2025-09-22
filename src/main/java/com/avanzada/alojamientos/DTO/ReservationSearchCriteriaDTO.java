package com.avanzada.alojamientos.DTO;

import com.avanzada.alojamientos.DTO.model.ReservationStatus;

public record ReservationSearchCriteriaDTO(
        String userId,
        String hostId,
        String accommodationId,
        ReservationStatus status,
        DateRangeDTO dateRange
) {}
