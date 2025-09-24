package com.avanzada.alojamientos.services;

import com.avanzada.alojamientos.DTO.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.List;
import java.util.Optional;

public interface AccommodationService {
    AccommodationDTO create(CreateAccommodationDTO dto, Long hostId);
    AccommodationDTO update(Long accommodationId, UpdateAccommodationDTO dto);
    Optional<AccommodationDTO> findById(Long accommodationId);

    // Búsqueda con criterios avanzados (ciudad, fechas, precio, servicios)
    Page<AccommodationDTO> search(AccommodationSearchCriteriaDTO criteria, Pageable pageable);

    void delete(String accommodationId);
    Page<AccommodationDTO> findByHost(Long hostId, Pageable pageable);

    // Gestión de imágenes
    void addImage(Long accommodationId, List<String> fileUrls, boolean primary);
    void removeImage(Long accommodationId, String imageUrl);

    // Métricas (reservas, promedio de calificaciones, filtrado por fechas)
    AccommodationMetricsDTO getMetrics(String accommodationId, DateRangeDTO range);
}
