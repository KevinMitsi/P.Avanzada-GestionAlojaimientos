package com.avanzada.alojamientos.controllers;

import com.avanzada.alojamientos.DTO.other.DateRange;
import com.avanzada.alojamientos.DTO.accommodation.*;
import com.avanzada.alojamientos.services.AccommodationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/accommodations")
@RequiredArgsConstructor
public class AccommodationController {

    private final AccommodationService accommodationService;

    @PostMapping("/{hostId}")
    public ResponseEntity<AccommodationDTO> create(@PathVariable Long hostId, @RequestBody @Valid CreateAccommodationDTO dto) {
        AccommodationDTO result = accommodationService.create(dto, hostId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/{accommodationId}")
    public ResponseEntity<AccommodationDTO> update(@PathVariable Long accommodationId,
                                   @RequestBody @Valid UpdateAccommodationDTO dto) {
        AccommodationDTO result = accommodationService.update(accommodationId, dto);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{accommodationId}")
    public ResponseEntity<AccommodationDTO> findById(@PathVariable Long accommodationId) {
        Optional<AccommodationDTO> result = accommodationService.findById(accommodationId);
        return result.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/search")
    public ResponseEntity<Page<AccommodationDTO>> search(@RequestBody AccommodationSearch criteria, Pageable pageable) {
        Page<AccommodationDTO> result = accommodationService.search(criteria, pageable);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{accommodationId}")
    public ResponseEntity<Void> delete(@PathVariable Long accommodationId) {
        accommodationService.delete(accommodationId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/host/{hostId}")
    public ResponseEntity<Page<AccommodationDTO>> findByHost(@PathVariable Long hostId, Pageable pageable) {
        Page<AccommodationDTO> result = accommodationService.findByHost(hostId, pageable);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{accommodationId}/images")
    public ResponseEntity<Void> addImage(@PathVariable Long accommodationId,
                         @RequestBody List<String> fileUrls,
                         @RequestParam(defaultValue = "false") boolean primary) {
        accommodationService.addImage(accommodationId, fileUrls, primary);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{accommodationId}/images/{imageUrl}")
    public ResponseEntity<Void> removeImage(@PathVariable Long accommodationId, @PathVariable String imageUrl) {
        accommodationService.removeImage(accommodationId, imageUrl);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{accommodationId}/metrics")
    public ResponseEntity<AccommodationMetrics> getMetrics(@PathVariable Long accommodationId,
                                           @RequestBody DateRange range) {
        AccommodationMetrics result = accommodationService.getMetrics(accommodationId, range);
        return ResponseEntity.ok(result);
    }
}
