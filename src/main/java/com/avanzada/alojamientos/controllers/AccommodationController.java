package com.avanzada.alojamientos.controllers;

import com.avanzada.alojamientos.DTO.*;
import com.avanzada.alojamientos.services.AccommodationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/accommodations")
@RequiredArgsConstructor
public class AccommodationController {

    private final AccommodationService accommodationService;

    @PostMapping("/{hostId}")
    public AccommodationDTO create(@PathVariable Long hostId, @RequestBody @Valid CreateAccommodationDTO dto) {
        return accommodationService.create(dto, hostId);
    }

    @PutMapping("/{accommodationId}")
    public AccommodationDTO update(@PathVariable Long accommodationId,
                                   @RequestBody @Valid UpdateAccommodationDTO dto) {
        return accommodationService.update(accommodationId, dto);
    }

    @GetMapping("/{accommodationId}")
    public Optional<AccommodationDTO> findById(@PathVariable Long accommodationId) {
        return accommodationService.findById(accommodationId);
    }

    @PostMapping("/search")
    public Page<AccommodationDTO> search(@RequestBody AccommodationSearchCriteriaDTO criteria, Pageable pageable) {
        return accommodationService.search(criteria, pageable);
    }

    @DeleteMapping("/{accommodationId}")
    public void delete(@PathVariable String accommodationId) {
        accommodationService.delete(accommodationId);
    }

    @GetMapping("/host/{hostId}")
    public Page<AccommodationDTO> findByHost(@PathVariable Long hostId, Pageable pageable) {
        return accommodationService.findByHost(hostId, pageable);
    }

    @PostMapping("/{accommodationId}/images")
    public void addImage(@PathVariable Long accommodationId,
                         @RequestBody List<String> fileUrls,
                         @RequestParam(defaultValue = "false") boolean primary) {
        accommodationService.addImage(accommodationId, fileUrls, primary);
    }

    @DeleteMapping("/{accommodationId}/images/{imageId}")
    public void removeImage(@PathVariable Long accommodationId, @PathVariable String imageId) {
        accommodationService.removeImage(accommodationId, imageId);
    }

    @PostMapping("/{accommodationId}/metrics")
    public AccommodationMetricsDTO getMetrics(@PathVariable String accommodationId,
                                              @RequestBody DateRangeDTO range) {
        return accommodationService.getMetrics(accommodationId, range);
    }
}

