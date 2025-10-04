package com.avanzada.alojamientos.services.impl;

import com.avanzada.alojamientos.DTO.model.CancelledBy;
import com.avanzada.alojamientos.DTO.model.UserRole;
import com.avanzada.alojamientos.DTO.reservation.CreateReservationDTO;
import com.avanzada.alojamientos.DTO.reservation.ReservationDTO;
import com.avanzada.alojamientos.DTO.reservation.ReservationSearchCriteria;
import com.avanzada.alojamientos.DTO.model.ReservationStatus;
import com.avanzada.alojamientos.entities.AccommodationEntity;
import com.avanzada.alojamientos.entities.ReservationEntity;
import com.avanzada.alojamientos.entities.UserEntity;
import com.avanzada.alojamientos.mappers.ReservationMapper;
import com.avanzada.alojamientos.repositories.AccommodationRepository;
import com.avanzada.alojamientos.repositories.ReservationRepository;
import com.avanzada.alojamientos.repositories.UserRepository;
import com.avanzada.alojamientos.services.NotificationService;
import com.avanzada.alojamientos.services.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final AccommodationRepository accommodationRepository;
    private final UserRepository userRepository;
    private final ReservationMapper mapper;
    private final NotificationService notificationService;


    @Override
    public ReservationDTO create(Long userId, CreateReservationDTO dto) {


        log.info("Creating reservation for user {} in accommodation {}",
                userId, dto.accommodationId());

        // 1. Validar que quien reserva es un USER (no un HOST)
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        if (user.getRole() != UserRole.USER) {
            throw new IllegalStateException("Only users with role USER can make reservations");
        }

        // 2. Validar que el alojamiento existe y no está eliminado
        AccommodationEntity accommodation = accommodationRepository.findById(dto.accommodationId())
                .orElseThrow(() -> new NoSuchElementException("Accommodation not found"));

        if (Boolean.TRUE.equals(accommodation.getSoftDeleted())) {
            throw new IllegalStateException("Cannot reserve a deleted accommodation");
        }

        // 3. Validar que el usuario no está reservando su propio alojamiento
        if (accommodation.getHost().getId().equals(userId)) {
            throw new IllegalStateException("You cannot reserve your own accommodation");
        }

        // 4. Validar fechas no sean pasadas
        if (dto.startDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot reserve past dates");
        }

        // 5. Validar mínimo 1 noche
        long nights = ChronoUnit.DAYS.between(dto.startDate(), dto.endDate());
        if (nights < 1) {
            throw new IllegalArgumentException("Minimum stay is 1 night");
        }

        // 6. Validar capacidad máxima de huéspedes
        Integer guests = dto.guests(); // Asumiendo que agregaste este campo al DTO
        if (guests > accommodation.getMaxGuests()) {
            throw new IllegalArgumentException(
                    String.format("Maximum capacity is %d guests", accommodation.getMaxGuests())
            );
        }

        // 7. Validar disponibilidad (no solapamiento)
        if (!isAvailable(dto.accommodationId(), dto.startDate(), dto.endDate(), guests)) {
            throw new IllegalStateException("Accommodation not available for selected dates");
        }

        // 8. Calcular precio total
        BigDecimal totalPrice = calculatePrice(
                dto.accommodationId(),
                dto.startDate(),
                dto.endDate(),
                guests
        );

        // 9. Crear la reserva
        ReservationEntity entity = mapper.toEntity(dto);
        entity.setAccommodation(accommodation);
        entity.setUser(user);
        entity.setNights((int) nights);
        entity.setTotalPrice(totalPrice);
        entity.setStatus(ReservationStatus.PENDING);
        entity.setCreatedAt(LocalDateTime.now());

        ReservationEntity saved = reservationRepository.save(entity);

        // 10. Notificar al anfitrión


        // 11. Enviar email de confirmación al usuario


        log.info("Reservation {} created successfully", saved.getId());
        return mapper.toDTO(saved);

    }

    @Transactional(readOnly = true)
    @Override
    public Optional<ReservationDTO> findById(Long reservationId) {
        return reservationRepository.findById(reservationId).map(mapper::toDTO);
    }

    @Transactional
    @Override
    public Page<ReservationDTO> findByUser(Long userId, Pageable pageable) {
        return reservationRepository.findByUser(userId, pageable)
                .map(mapper::toDTO);
    }

    @Transactional
    @Override
    public Page<ReservationDTO> findByAccommodation(Long accommodationId, Pageable pageable) {
        return reservationRepository.findByAccommodation(accommodationId, pageable)
                .map(mapper::toDTO);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<ReservationDTO> searchReservations(ReservationSearchCriteria criteria, Pageable pageable) {
        LocalDate startDate = (criteria.dateRange() != null) ? criteria.dateRange().startDate() : null;
        LocalDate endDate   = (criteria.dateRange() != null) ? criteria.dateRange().endDate()   : null;

        // Si no viene ordenamiento, se ordena por startDate asc
        if (pageable.getSort().isUnsorted()) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("startDate").ascending());
        }

        return reservationRepository.searchReservations(
                criteria.userId(),
                criteria.hostId(),
                criteria.accommodationId(),
                criteria.status(),
                startDate,
                endDate,
                pageable
        ).map(mapper::toDTO);
    }


    @Override
    public boolean isAvailable(Long accommodationId, LocalDate start, LocalDate end, int guests) {
        List<ReservationEntity> overlapping = reservationRepository
                .findOverlappingReservations(accommodationId, start, end);
        return overlapping.isEmpty();
    }

    @Override
    public BigDecimal calculatePrice(Long accommodationId, LocalDate start, LocalDate end, int guests) {
        AccommodationEntity accommodation = accommodationRepository
                .findById(accommodationId)
                .orElseThrow(() -> new NoSuchElementException("Accommodation not found"));

        long nights = ChronoUnit.DAYS.between(start, end);
        return accommodation.getPricePerNight().multiply(BigDecimal.valueOf(nights));
    }

    @Transactional
    @Override
    public void cancel(Long reservationId, Long cancelledByUserId, String motivo) {

        log.info("Cancelling reservation {} by user {}", reservationId, cancelledByUserId);

        ReservationEntity reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NoSuchElementException("Reservation not found"));

        UserEntity cancelledBy = userRepository.findById(cancelledByUserId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        // Validar que quien cancela es el dueño de la reserva o el host
        boolean isGuest = reservation.getUser().getId().equals(cancelledByUserId);
        boolean isHost = reservation.getAccommodation().getHost().getId().equals(cancelledByUserId);

        if (!isGuest && !isHost) {
            throw new IllegalStateException("You don't have permission to cancel this reservation");
        }

        // Validar estado actual
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("Reservation is already cancelled");
        }

        if (reservation.getStatus() == ReservationStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a completed reservation");
        }

        // Si es el usuario (huésped), validar 48 horas antes
        if (isGuest) {
            LocalDateTime checkInDateTime = reservation.getStartDate().atStartOfDay();
            LocalDateTime now = LocalDateTime.now();
            long hoursUntilCheckIn = ChronoUnit.HOURS.between(now, checkInDateTime);

            if (hoursUntilCheckIn < 48) {
                throw new IllegalStateException(
                        "Cancellation must be made at least 48 hours before check-in"
                );
            }
        }

        // Actualizar reserva
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setCancelledAt(LocalDateTime.now());
        reservation.setMotivoCancelacion(motivo);
        reservation.setCancelledBy(isGuest ? CancelledBy.USER : CancelledBy.HOST);

        reservationRepository.save(reservation);



        log.info("Reservation {} cancelled by {}", reservationId,
                isGuest ? "guest" : "host");

    }
    @Transactional
    @Override
    public void updateStatus(Long reservationId, ReservationStatus status, Long hostId) {

        log.info("Host {} updating reservation {} to status {}",
                hostId, reservationId, status);

        ReservationEntity reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NoSuchElementException("Reservation not found"));

        // Validar que quien actualiza es el dueño del alojamiento
        if (!reservation.getAccommodation().getHost().getId().equals(hostId)) {
            throw new IllegalStateException("You don't have permission to update this reservation");
        }

        // Validar transiciones de estado válidas
        ReservationStatus currentStatus = reservation.getStatus();

        if (currentStatus == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("Cannot change status of cancelled reservation");
        }

        if (currentStatus == ReservationStatus.COMPLETED) {
            throw new IllegalStateException("Cannot change status of completed reservation");
        }

        // Si está en PENDING, solo puede pasar a CONFIRMED o CANCELLED
        if (currentStatus == ReservationStatus.PENDING) {
            if (status != ReservationStatus.CONFIRMED && status != ReservationStatus.CANCELLED) {
                throw new IllegalArgumentException("Can only confirm or cancel pending reservations");
            }
        }

        reservation.setStatus(status);
        reservationRepository.save(reservation);



        log.info("Reservation {} status updated to {}", reservationId, status);

    }
}
