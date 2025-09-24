package com.avanzada.alojamientos.services.impl;

import com.avanzada.alojamientos.DTO.*;
import com.avanzada.alojamientos.services.AccommodationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class AccommodationServiceImpl implements AccommodationService {


    @Override
    public AccommodationDTO create(CreateAccommodationDTO dto, Long hostId) {
        return null;
    }

    @Override
    public AccommodationDTO update(Long accommodationId, UpdateAccommodationDTO dto) {
        return null;
    }

    @Override
    public Optional<AccommodationDTO> findById(Long accommodationId) {
        return Optional.empty();
    }

    @Override
    public Page<AccommodationDTO> search(AccommodationSearchCriteriaDTO criteria, Pageable pageable) {
        return null;
    }

    @Override
    public void delete(String accommodationId) {
        log.info("Deleting accommodation with ID: {}", accommodationId);
    }

    @Override
    public Page<AccommodationDTO> findByHost(Long hostId, Pageable pageable) {
        return null;
    }

    @Override
    public void addImage(Long accommodationId, List<String> fileUrls, boolean primary) {
        log.info("Adding images to accommodation ID: {}. Primary: {}. URLs: {}", accommodationId, primary, fileUrls);
    }

    @Override
    public void removeImage(Long accommodationId, String imageUrl) {
        log.info("Removing image from accommodation ID: {}. Image URL: {}", accommodationId, imageUrl);
    }

    @Override
    public AccommodationMetricsDTO getMetrics(String accommodationId, DateRangeDTO range) {
        return null;
    }
}
