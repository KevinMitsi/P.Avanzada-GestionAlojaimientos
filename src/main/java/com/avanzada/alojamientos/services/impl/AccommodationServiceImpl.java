package com.avanzada.alojamientos.services.impl;

import com.avanzada.alojamientos.DTO.accommodation.*;
import com.avanzada.alojamientos.DTO.other.DateRange;
import com.avanzada.alojamientos.mappers.AccommodationMapper;
import com.avanzada.alojamientos.repositories.AccommodationRepository;
import com.avanzada.alojamientos.services.AccommodationService;
import com.avanzada.alojamientos.entities.AccommodationEntity;
import com.avanzada.alojamientos.entities.ImageEntity;
import com.avanzada.alojamientos.entities.ReservationEntity;
import com.avanzada.alojamientos.entities.UserEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccommodationServiceImpl implements AccommodationService {

    private static final int MAX_IMAGES = 10;

    private final AccommodationRepository accommodationRepository;
    private final AccommodationMapper accommodationMapper;

    /**
     * DTO validations (Jakarta Bean Validation) expected:
     * - title non-null / not blank
     * - description length limit
     * - pricePerNight positive
     * - maxGuests positive
     */
    @Override
    @Transactional
    public AccommodationDTO create(CreateAccommodationDTO dto, Long hostId) {
        AccommodationEntity entity = accommodationMapper.toEntity(dto);

        if (hostId != null) {
            UserEntity host = new UserEntity();
            host.setId(hostId);
            entity.setHost(host);
        }

        setDefaultValues(entity);

        AccommodationEntity savedEntity = accommodationRepository.save(entity);
        return accommodationMapper.toAccommodationDTO(savedEntity);
    }

    @Override
    @Transactional
    public AccommodationDTO update(Long accommodationId, UpdateAccommodationDTO dto) {
        validateAccommodationId(accommodationId);

        AccommodationEntity entity = findAccommodationEntity(accommodationId);
        validateNotDeleted(entity);

        accommodationMapper.updateEntityFromDTO(dto, entity);

        AccommodationEntity savedEntity = accommodationRepository.save(entity);
        return accommodationMapper.toAccommodationDTO(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AccommodationDTO> findById(Long accommodationId) {
        if (accommodationId == null) {
            return Optional.empty();
        }

        return accommodationRepository.findById(accommodationId)
                .filter(accommodation -> !Boolean.TRUE.equals(accommodation.getSoftDeleted()))
                .map(accommodationMapper::toAccommodationDTO);
    }

    @Override
    public Page<AccommodationDTO> search(AccommodationSearch criteria, Pageable pageable) {
        Page<AccommodationEntity> entityPage = performSearch(criteria, pageable);

        List<AccommodationDTO> dtos = entityPage.stream()
                .filter(accommodation -> !Boolean.TRUE.equals(accommodation.getSoftDeleted()))
                .map(accommodationMapper::toAccommodationDTO)
                .toList();

        return new PageImpl<>(dtos, pageable, entityPage.getTotalElements());
    }

    @Override
    @Transactional
    public void delete(Long accommodationId) {
        if (accommodationId == null) {
            log.warn("delete called with null id");
            return;
        }

        if (!accommodationRepository.existsById(accommodationId)) {
            log.warn("Attempt to delete non-existing accommodation: {}", accommodationId);
            return;
        }

        validateNoFutureReservations(accommodationId);

        AccommodationEntity entity = findAccommodationEntity(accommodationId);
        performSoftDelete(entity);

        log.info("Soft deleted accommodation {}", accommodationId);
    }

    @Override
    public Page<AccommodationDTO> findByHost(Long hostId, Pageable pageable) {
        if (hostId == null) {
            return Page.empty(pageable);
        }

        Page<AccommodationEntity> entityPage = findAccommodationsByHost(hostId, pageable);

        List<AccommodationDTO> dtos = entityPage.stream()
                .filter(accommodation -> isHostMatch(accommodation, hostId))
                .map(accommodationMapper::toAccommodationDTO)
                .toList();

        return new PageImpl<>(dtos, pageable, entityPage.getTotalElements());
    }

    @Override
    @Transactional
    public void addImage(Long accommodationId, List<String> fileUrls, boolean primary) {
        if (fileUrls == null || fileUrls.isEmpty()) {
            return;
        }

        AccommodationEntity accommodation = findAccommodationEntity(accommodationId);
        validateNotDeleted(accommodation);

        List<ImageEntity> currentImages = getCurrentImages(accommodation);
        validateImageCapacity(currentImages);

        if (primary) {
            setPrimaryToFalse(currentImages);
        }

        List<ImageEntity> newImages = createNewImages(fileUrls, accommodation, primary, currentImages.size());
        currentImages.addAll(newImages);

        accommodation.setImages(currentImages);
        accommodationRepository.save(accommodation);
    }

    @Override
    @Transactional
    public void removeImage(Long accommodationId, String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        AccommodationEntity accommodation = findAccommodationEntity(accommodationId);
        List<ImageEntity> currentImages = getCurrentImages(accommodation);

        List<ImageEntity> filteredImages = currentImages.stream()
                .filter(image -> !imageUrl.equals(image.getUrl()))
                .toList();

        if (filteredImages.size() == currentImages.size()) {
            log.warn("Image url {} not found in accommodation {}", imageUrl, accommodationId);
            return;
        }

        accommodation.setImages(filteredImages);
        accommodationRepository.save(accommodation);
    }

    @Override
    public AccommodationMetrics getMetrics(Long accommodationId, DateRange range) {
        validateMetricsParameters(accommodationId, range);

        AccommodationEntity accommodation = findAccommodationEntity(accommodationId);
        List<ReservationEntity> reservations = getReservations(accommodation);

        long totalReservations = countValidReservations(reservations, range);
        double averageRating = calculateAverageRating(accommodation);
        BigDecimal totalRevenue = calculateTotalRevenue(reservations, range);

        return new AccommodationMetrics(totalReservations, averageRating, totalRevenue);
    }

    // Private helper methods

    private void setDefaultValues(AccommodationEntity entity) {
        if (entity.getActive() == null) {
            entity.setActive(true);
        }
        if (entity.getSoftDeleted() == null) {
            entity.setSoftDeleted(false);
        }
    }

    private void validateAccommodationId(Long accommodationId) {
        if (accommodationId == null) {
            throw new IllegalArgumentException("accommodationId is required");
        }

        if (!accommodationRepository.existsById(accommodationId)) {
            log.warn("Accommodation not found: {}", accommodationId);
            throw new NoSuchElementException("Accommodation not found: " + accommodationId);
        }
    }

    private AccommodationEntity findAccommodationEntity(Long accommodationId) {
        return accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new NoSuchElementException("Accommodation not found: " + accommodationId));
    }

    private void validateNotDeleted(AccommodationEntity entity) {
        if (Boolean.TRUE.equals(entity.getSoftDeleted())) {
            throw new IllegalStateException("Cannot operate on deleted accommodation");
        }
    }

    private Page<AccommodationEntity> performSearch(AccommodationSearch criteria, Pageable pageable) {
        try {
            if (hasServicesFilter(criteria)) {
                return accommodationRepository.searchWithServices(
                        criteria.cityId(),
                        criteria.minPrice(),
                        criteria.maxPrice(),
                        criteria.guests(),
                        criteria.startDate(),
                        criteria.endDate(),
                        criteria.services(),
                        criteria.services().size(),
                        pageable
                );
            } else {
                return accommodationRepository.search(
                        criteria == null ? null : criteria.cityId(),
                        criteria == null ? null : criteria.minPrice(),
                        criteria == null ? null : criteria.maxPrice(),
                        criteria == null ? null : criteria.guests(),
                        criteria == null ? null : criteria.startDate(),
                        criteria == null ? null : criteria.endDate(),
                        pageable
                );
            }
        } catch (Exception ex) {
            log.debug("Repository search methods not available or failed, falling back to findAll(pageable). Reason: {}", ex.getMessage());
            return accommodationRepository.findAll(pageable);
        }
    }

    private boolean hasServicesFilter(AccommodationSearch criteria) {
        return criteria != null && criteria.services() != null && !criteria.services().isEmpty();
    }

    private void validateNoFutureReservations(Long accommodationId) {
        long futureReservationsCount = countFutureReservations(accommodationId);

        if (futureReservationsCount > 0) {
            throw new IllegalStateException("Accommodation has future reservations and cannot be deleted.");
        }

        // Fallback validation if repository count method is not available
        if (futureReservationsCount == 0L) {
            AccommodationEntity entity = findAccommodationEntity(accommodationId);
            validateNoFutureReservationsFallback(entity);
        }
    }

    private long countFutureReservations(Long accommodationId) {
        try {
            return accommodationRepository.countFutureNonCancelledReservations(accommodationId, LocalDate.now());
        } catch (Exception e) {
            log.debug("countFutureNonCancelledReservations not available in repository, will fallback to entity inspection");
            return 0L;
        }
    }

    private void validateNoFutureReservationsFallback(AccommodationEntity entity) {
        if (entity.getReservations() != null && !entity.getReservations().isEmpty()) {
            boolean hasFutureReservations = entity.getReservations().stream()
                    .anyMatch(reservation -> {
                        LocalDate startDate = reservation.getStartDate();
                        return startDate != null && startDate.isAfter(LocalDate.now());
                    });

            if (hasFutureReservations) {
                throw new IllegalStateException("Accommodation has future reservations and cannot be deleted.");
            }
        }
    }

    private void performSoftDelete(AccommodationEntity entity) {
        entity.setSoftDeleted(true);
        entity.setActive(false);
        entity.setDeletedAt(LocalDateTime.now());
        accommodationRepository.save(entity);
    }

    private Page<AccommodationEntity> findAccommodationsByHost(Long hostId, Pageable pageable) {
        try {
            return accommodationRepository.findByHostIdAndSoftDeletedFalse(hostId, pageable);
        } catch (Exception ex) {
            log.debug("Repository.findByHostIdAndSoftDeletedFalse not available, falling back to findAll. Reason: {}", ex.getMessage());
            return accommodationRepository.findAll(pageable);
        }
    }

    private boolean isHostMatch(AccommodationEntity accommodation, Long hostId) {
        return accommodation.getHost() != null && Objects.equals(accommodation.getHost().getId(), hostId);
    }

    private List<ImageEntity> getCurrentImages(AccommodationEntity accommodation) {
        return accommodation.getImages() != null ? accommodation.getImages() : new ArrayList<>();
    }

    private void validateImageCapacity(List<ImageEntity> currentImages) {
        int remainingSpace = MAX_IMAGES - currentImages.size();
        if (remainingSpace <= 0) {
            throw new IllegalStateException("Max images reached: " + MAX_IMAGES);
        }
    }

    private void setPrimaryToFalse(List<ImageEntity> images) {
        images.forEach(image -> image.setIsPrimary(false));
    }

    private List<ImageEntity> createNewImages(List<String> fileUrls, AccommodationEntity accommodation,
                                              boolean primary, int currentImageCount) {
        int remainingSpace = MAX_IMAGES - currentImageCount;
        int imagesToAdd = Math.min(remainingSpace, fileUrls.size());

        List<ImageEntity> newImages = new ArrayList<>();

        for (int i = 0; i < imagesToAdd; i++) {
            String url = fileUrls.get(i);
            ImageEntity image = createImageEntity(url, primary && i == 0, accommodation);
            newImages.add(image);
        }

        return newImages;
    }

    private ImageEntity createImageEntity(String url, boolean isPrimary, AccommodationEntity accommodation) {
        ImageEntity image = new ImageEntity();
        image.setUrl(url);
        image.setIsPrimary(isPrimary);
        image.setCreatedAt(LocalDateTime.now());
        image.setAccommodation(accommodation);
        return image;
    }

    private void validateMetricsParameters(Long accommodationId, DateRange range) {
        if (accommodationId == null || range == null) {
            throw new IllegalArgumentException("Accommodation id and range are required");
        }
    }

    private List<ReservationEntity> getReservations(AccommodationEntity accommodation) {
        return accommodation.getReservations() != null ? accommodation.getReservations() : Collections.emptyList();
    }

    private long countValidReservations(List<ReservationEntity> reservations, DateRange range) {
        return getValidReservationsStream(reservations, range).count();
    }

    private double calculateAverageRating(AccommodationEntity accommodation) {
        if (accommodation.getComments() == null || accommodation.getComments().isEmpty()) {
            return 0.0;
        }

        return accommodation.getComments().stream()
                .mapToInt(comment -> comment.getRating() != null ? comment.getRating() : 0)
                .average()
                .orElse(0.0);
    }

    private BigDecimal calculateTotalRevenue(List<ReservationEntity> reservations, DateRange range) {
        return getValidReservationsStream(reservations, range)
                .map(ReservationEntity::getTotalPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Stream<ReservationEntity> getValidReservationsStream(List<ReservationEntity> reservations, DateRange range) {
        LocalDate startDate = range.startDate();
        LocalDate endDate = range.endDate();

        return reservations.stream()
                .filter(reservation -> isReservationInDateRange(reservation, startDate, endDate))
                .filter(this::isReservationNotCancelled);
    }

    private boolean isReservationInDateRange(ReservationEntity reservation, LocalDate rangeStart, LocalDate rangeEnd) {
        LocalDate reservationStart = reservation.getStartDate();
        LocalDate reservationEnd = reservation.getEndDate();

        return reservationStart != null && !reservationStart.isAfter(rangeEnd)
                && reservationEnd != null && !reservationEnd.isBefore(rangeStart);
    }

    private boolean isReservationNotCancelled(ReservationEntity reservation) {
        if (reservation.getStatus() == null) {
            return true;
        }

        try {
            String statusName = reservation.getStatus().name().toUpperCase();
            return !statusName.contains("CANCEL");
        } catch (Exception ex) {
            return true;
        }
    }
}
