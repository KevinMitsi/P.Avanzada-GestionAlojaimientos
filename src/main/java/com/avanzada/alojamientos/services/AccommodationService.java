package com.avanzada.alojamientos.services;

import com.avanzada.alojamientos.DTO.accommodation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AccommodationService {
    CreateAccommodationResponseDTO create(CreateAccommodationDTO dto, Long hostId);
    AccommodationDTO update(Long userId, Long accommodationId, UpdateAccommodationDTO dto);
    Optional<AccommodationDTO> findById(Long accommodationId);
    Page<AccommodationFoundDTO> search(AccommodationSearch criteria, Pageable pageable);
    void delete(Long userId, Long accommodationId);
    Page<AccommodationDTO> findByHost(Long hostId, Pageable pageable);
    AccommodationMetrics getMetrics(Long accommodationId, LocalDate start, LocalDate end);
    List<String> getAllServices();
}
