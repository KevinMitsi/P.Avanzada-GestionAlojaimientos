package com.avanzada.alojamientos.services.impl;

import com.avanzada.alojamientos.DTO.accommodation.*;

import com.avanzada.alojamientos.exceptions.SearchingAccommodationException;
import com.avanzada.alojamientos.mappers.AccommodationMapper;
import com.avanzada.alojamientos.repositories.AccommodationRepository;
import com.avanzada.alojamientos.services.AccommodationService;
import com.avanzada.alojamientos.services.ImageService;
import com.avanzada.alojamientos.entities.AccommodationEntity;
import com.avanzada.alojamientos.entities.ImageEntity;
import com.avanzada.alojamientos.entities.ReservationEntity;
import com.avanzada.alojamientos.entities.UserEntity;
import com.avanzada.alojamientos.exceptions.UploadingImageException;
import com.avanzada.alojamientos.exceptions.DeletingImageException;
import com.avanzada.alojamientos.repositories.ImageRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    public static final String ACCOMMODATION_NOT_FOUND_MESSAGE = "Accommodation not found: ";

    private final AccommodationRepository accommodationRepository;
    private final AccommodationMapper accommodationMapper;
    private final ImageService imageService;
    private final ImageRepository imageRepository;


    @Override
    @Transactional
    public CreateAccommodationResponseDTO create(CreateAccommodationDTO dto, Long hostId) {
        AccommodationEntity entity = accommodationMapper.toEntity(dto);

        if (hostId != null) {
            UserEntity host = new UserEntity();
            host.setId(hostId);
            entity.setHost(host);
        }

        setDefaultValues(entity);

        AccommodationEntity savedEntity = accommodationRepository.save(entity);
        return accommodationMapper.toCreateAccommodationResponseDTO(savedEntity);
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
    @Transactional(readOnly = true)
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
    public AccommodationMetrics getMetrics(Long accommodationId, LocalDate start, LocalDate end) {
        // Cargar accommodation con reservations para calcular métricas de reservas y revenue
        AccommodationEntity accommodationWithReservations = accommodationRepository
                .findByIdWithReservations(accommodationId)
                .orElseThrow(() -> new NoSuchElementException(ACCOMMODATION_NOT_FOUND_MESSAGE + accommodationId));

        // Cargar accommodation con comments para calcular rating promedio
        AccommodationEntity accommodationWithComments = accommodationRepository
                .findByIdWithComments(accommodationId)
                .orElseThrow(() -> new NoSuchElementException(ACCOMMODATION_NOT_FOUND_MESSAGE + accommodationId));

        List<ReservationEntity> reservations = getReservations(accommodationWithReservations);

        long totalReservations = countValidReservations(reservations, start, end);
        double averageRating = calculateAverageRating(accommodationWithComments);
        BigDecimal totalRevenue = calculateTotalRevenue(reservations, start, end);

        return new AccommodationMetrics(totalReservations, averageRating, totalRevenue);
    }


    @Transactional
    public List<String> uploadAndAddImages(Long accommodationId, List<MultipartFile> imageFiles, boolean primary) throws UploadingImageException {
        if (imageFiles == null || imageFiles.isEmpty()) {
            return Collections.emptyList();
        }

        AccommodationEntity accommodation = findAccommodationEntity(accommodationId);
        validateNotDeleted(accommodation);

        List<ImageEntity> currentImages = getCurrentImages(accommodation);
        validateImageCapacity(currentImages, imageFiles.size());

        if (primary) {
            setPrimaryToFalse(currentImages);
        }

        List<String> uploadedUrls = new ArrayList<>();
        List<ImageEntity> newImages = new ArrayList<>();

        for (int i = 0; i < imageFiles.size(); i++) {
            MultipartFile file = imageFiles.get(i);
            try {
                Map<Object, Object> uploadResult = imageService.upload(file);
                String imageUrl = (String) uploadResult.get("secure_url");
                String publicId = (String) uploadResult.get("public_id");
                String thumbnailUrl = (String) uploadResult.get("eager");

                ImageEntity image = createImageEntityFromCloudinary(
                    imageUrl,
                    publicId,
                    thumbnailUrl,
                    primary && i == 0,
                    accommodation
                );
                newImages.add(image);
                uploadedUrls.add(imageUrl);

            } catch (UploadingImageException e) {
                log.error("Error uploading image for accommodation {}: {}", accommodationId, e.getMessage());
                throw e;
            }
        }

        currentImages.addAll(newImages);
        accommodation.setImages(currentImages);
        accommodationRepository.save(accommodation);

        return uploadedUrls;
    }


    @Transactional
    public void deleteImageFromCloudinary(Long accommodationId, Long imageId) throws DeletingImageException {
        if (imageId == null) {
            throw new DeletingImageException("imageId is required");
        }

        // Verificar que el accommodation existe
        if (Boolean.FALSE.equals(accommodationRepository.existsByIdAndSoftDeletedFalse(accommodationId))) {
            throw new DeletingImageException("Accommodation not found with ID: " + accommodationId);
        }

        ImageEntity image = imageRepository.findById(imageId)
                .orElseThrow(() -> new DeletingImageException("Image not found with ID: " + imageId));

        if (!Objects.equals(image.getAccommodation().getId(), accommodationId)) {
            throw new DeletingImageException("Image does not belong to the specified accommodation");
        }

        try {
            if (image.getCloudinaryPublicId() != null) {
                imageService.delete(image.getCloudinaryPublicId());
            }
            imageRepository.delete(image);

            log.info("Successfully deleted image with ID {} from accommodation {}", imageId, accommodationId);

        } catch (DeletingImageException e) {
            log.error("Error deleting image from Cloudinary: {}", e.getMessage());
            throw e;
        }
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
            throw new NoSuchElementException(ACCOMMODATION_NOT_FOUND_MESSAGE + accommodationId);
        }
    }

    private AccommodationEntity findAccommodationEntity(Long accommodationId) {
        return accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new NoSuchElementException(ACCOMMODATION_NOT_FOUND_MESSAGE + accommodationId));
    }

    private void validateNotDeleted(AccommodationEntity entity) {
        if (Boolean.TRUE.equals(entity.getSoftDeleted())) {
            throw new IllegalStateException("Cannot operate on deleted accommodation");
        }
    }

    private Page<AccommodationEntity> performSearch(AccommodationSearch criteria, Pageable pageable) {
        Page<AccommodationEntity> entityPage;
        if (hasServicesFilter(criteria)) {
            log.debug("Searching accommodations with services filter: {}", criteria.services());
            try {
                entityPage = createCriteriaAndSearch(criteria, pageable, false);
            } catch (Exception ex) {
                log.error("Error executing searchWithServices query with criteria: {}. Error: {}", criteria, ex.getMessage(), ex);
                throw new SearchingAccommodationException("Error searching accommodations with services filter "+  ex);
            }
        } else {
            log.debug("Searching accommodations without services filter");
            try {
                entityPage = createCriteriaAndSearch(criteria, pageable, true);
            } catch (Exception ex) {
                log.error("Error executing search query with criteria: {}. Error: {}", criteria, ex.getMessage(), ex);
                throw new SearchingAccommodationException("Error searching accommodations " + ex);
            }
        }

        return entityPage;
    }

    private Page<AccommodationEntity> createCriteriaAndSearch(AccommodationSearch criteria, Pageable pageable, boolean withoutServices) {
       if (withoutServices){
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

       // Para búsqueda con servicios, usar la nueva aproximación de dos consultas
       List<Long> accommodationIds = accommodationRepository.findAccommodationIdsWithServices(
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

       if (accommodationIds.isEmpty()) {
           return Page.empty(pageable);
       }

       // Cargar las entidades completas con EntityGraph
       List<AccommodationEntity> accommodations = accommodationRepository.findByIdsWithEntityGraph(accommodationIds);

       // Crear una Page manualmente
       return new PageImpl<>(accommodations, pageable, accommodations.size());
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

    private void validateImageCapacity(List<ImageEntity> currentImages, int additionalImages) {
        int remainingSpace = MAX_IMAGES - currentImages.size();
        if (remainingSpace <= 0 || remainingSpace < additionalImages) {
            throw new IllegalStateException("Max images reached: " + MAX_IMAGES);
        }
    }

    private void setPrimaryToFalse(List<ImageEntity> images) {
        images.forEach(image -> image.setIsPrimary(false));
    }



    private ImageEntity createImageEntityFromCloudinary(String url, String publicId, String thumbnailUrl,
                                                        boolean isPrimary, AccommodationEntity accommodation) {
        ImageEntity image = new ImageEntity();
        image.setUrl(url);
        image.setCloudinaryPublicId(publicId);
        image.setCloudinaryThumbnailUrl(thumbnailUrl);
        image.setIsPrimary(isPrimary);
        image.setCreatedAt(LocalDateTime.now());
        image.setAccommodation(accommodation);
        return image;
    }


    private List<ReservationEntity> getReservations(AccommodationEntity accommodation) {
        return accommodation.getReservations() != null ? accommodation.getReservations() : Collections.emptyList();
    }

    private long countValidReservations(List<ReservationEntity> reservations, LocalDate start, LocalDate end) {
        return getValidReservationsStream(reservations, start, end).count();
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

    private BigDecimal calculateTotalRevenue(List<ReservationEntity> reservations, LocalDate start, LocalDate end) {
        return getValidReservationsStream(reservations, start, end)
                .map(ReservationEntity::getTotalPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Stream<ReservationEntity> getValidReservationsStream(List<ReservationEntity> reservations, LocalDate start, LocalDate end) {

        return reservations.stream()
                .filter(reservation -> isReservationInDateRange(reservation, start, end))
                .filter(this::isReservationNotCancelled);
    }

    private boolean isReservationInDateRange(ReservationEntity reservation, LocalDate rangeStart, LocalDate rangeEnd) {
        LocalDate reservationStart = reservation.getStartDate();
        LocalDate reservationEnd = reservation.getEndDate();

        if (reservationStart == null || reservationEnd == null) {
            return false;
        }

        if (rangeStart == null && rangeEnd == null) {
            return true;
        }

        if (rangeStart != null && rangeEnd == null) {
            return !reservationEnd.isBefore(rangeStart);
        }

        if (rangeStart == null ) {
            return !reservationStart.isAfter(rangeEnd);
        }

        // Si ambas fechas del rango están definidas, hacer la validación completa
        return !reservationStart.isAfter(rangeEnd) && !reservationEnd.isBefore(rangeStart);
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
