package com.avanzada.alojamientos.services;

import com.avanzada.alojamientos.DTO.accommodation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;

public interface AccommodationService {
    AccommodationDTO create(CreateAccommodationDTO dto, Long hostId);
    AccommodationDTO update(Long accommodationId, UpdateAccommodationDTO dto);
    Optional<AccommodationDTO> findById(Long accommodationId);

    // Búsqueda con criterios avanzados (ciudad, fechas, precio, servicios)
    Page<AccommodationDTO> search(AccommodationSearch criteria, Pageable pageable);

    void delete(Long accommodationId);
    Page<AccommodationDTO> findByHost(Long hostId, Pageable pageable);
    // Métricas (reservas, promedio de calificaciones, filtrado por fechas)
    AccommodationMetrics getMetrics(Long accommodationId, LocalDate start, LocalDate end);
}
