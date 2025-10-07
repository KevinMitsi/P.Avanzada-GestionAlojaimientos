package com.avanzada.alojamientos.services;

import com.avanzada.alojamientos.DTO.accommodation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;

public interface AccommodationService {
    CreateAccommodationResponseDTO create(CreateAccommodationDTO dto, Long hostId);
    AccommodationDTO update(Long accommodationId, UpdateAccommodationDTO dto);
    Optional<AccommodationDTO> findById(Long accommodationId);
    Page<AccommodationDTO> search(AccommodationSearch criteria, Pageable pageable);
    void delete(Long accommodationId);
    Page<AccommodationDTO> findByHost(Long hostId, Pageable pageable);
    AccommodationMetrics getMetrics(Long accommodationId, LocalDate start, LocalDate end);
}
