package com.avanzada.alojamientos.controllers;


import com.avanzada.alojamientos.DTO.accommodation.*;
import com.avanzada.alojamientos.services.AccommodationService;
import com.avanzada.alojamientos.services.impl.AccommodationServiceImpl;
import com.avanzada.alojamientos.exceptions.UploadingStorageException;
import com.avanzada.alojamientos.exceptions.DeletingStorageException;
import com.avanzada.alojamientos.security.CurrentUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/accommodations")
@RequiredArgsConstructor
public class AccommodationController {

    private final AccommodationService accommodationService;
    private final CurrentUserService currentUserService;

    @PostMapping
    public ResponseEntity<CreateAccommodationResponseDTO> create(@RequestBody @Valid CreateAccommodationDTO dto) {
        Long hostId = currentUserService.getCurrentHostId();
        CreateAccommodationResponseDTO result = accommodationService.create(dto, hostId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/{accommodationId}")
    public ResponseEntity<AccommodationDTO> update(@PathVariable Long accommodationId,
                                   @RequestBody @Valid UpdateAccommodationDTO dto) {
        AccommodationDTO result = accommodationService.update(accommodationId, dto);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<AccommodationDTO>> search(
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Integer guests,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) List<String> services,
            Pageable pageable) {

        Page<AccommodationDTO> result = accommodationService.search(generateCriteria(cityId,
                startDate, endDate, guests, minPrice, maxPrice, services), pageable);
        return ResponseEntity.ok(result);
    }



    @DeleteMapping("/{accommodationId}")
    public ResponseEntity<Void> delete(@PathVariable Long accommodationId) {
        accommodationService.delete(accommodationId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/host/me")
    public ResponseEntity<Page<AccommodationDTO>> findByHost(Pageable pageable) {
        Long hostId = currentUserService.getCurrentHostId();
        Page<AccommodationDTO> result = accommodationService.findByHost(hostId, pageable);
        return ResponseEntity.ok(result);
    }


    @PostMapping("/{accommodationId}/images/upload")
    public ResponseEntity<List<String>> uploadImages(@PathVariable Long accommodationId,
                                     @RequestParam("images") List<MultipartFile> imageFiles,
                                     @RequestParam(defaultValue = "false") boolean primary) throws UploadingStorageException {
        List<String> uploadedUrls = ((AccommodationServiceImpl) accommodationService)
                    .uploadAndAddImages(accommodationId, imageFiles, primary);
        return ResponseEntity.ok(uploadedUrls);

    }

    @DeleteMapping("/{accommodationId}/images/{imageId}")
    public ResponseEntity<Void> deleteImageFromCloudinary(@PathVariable Long accommodationId,
                                           @PathVariable Long imageId) throws DeletingStorageException {

        ((AccommodationServiceImpl) accommodationService).deleteImageFromCloudinary(accommodationId, imageId);
        return ResponseEntity.noContent().build();
       
    }

    @GetMapping("/{accommodationId}/metrics")
    public ResponseEntity<AccommodationMetrics> getMetrics(@PathVariable Long accommodationId,
                                                           @RequestParam(name = "start", required = false) LocalDate startDate,
                                                           @RequestParam(name = "end", required = false) LocalDate endDate){
        AccommodationMetrics result = accommodationService.getMetrics(accommodationId, startDate, endDate);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{accommodationId}")
    public ResponseEntity<AccommodationDTO> findById(@PathVariable Long accommodationId) {
        Optional<AccommodationDTO> result = accommodationService.findById(accommodationId);
        return result.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }

    private static AccommodationSearch generateCriteria(Long cityId, String startDate, String endDate, Integer guests, BigDecimal minPrice, BigDecimal maxPrice, List<String> services) {
        return new AccommodationSearch(
                cityId,
                startDate != null ? LocalDate.parse(startDate) : null,
                endDate != null ? LocalDate.parse(endDate) : null,
                guests,
                minPrice,
                maxPrice,
                services
        );
    }
}
