package com.avanzada.alojamientos.services.impl;

import com.avanzada.alojamientos.DTO.model.CancelledBy;

import com.avanzada.alojamientos.DTO.reservation.CreateReservationDTO;
import com.avanzada.alojamientos.DTO.reservation.ReservationDTO;
import com.avanzada.alojamientos.DTO.reservation.ReservationSearchCriteria;
import com.avanzada.alojamientos.DTO.model.ReservationStatus;
import com.avanzada.alojamientos.DTO.notification.EmailDTO;
import com.avanzada.alojamientos.entities.AccommodationEntity;
import com.avanzada.alojamientos.entities.ReservationEntity;
import com.avanzada.alojamientos.entities.UserEntity;
import com.avanzada.alojamientos.exceptions.*;
import com.avanzada.alojamientos.mappers.ReservationMapper;
import com.avanzada.alojamientos.repositories.AccommodationRepository;
import com.avanzada.alojamientos.repositories.ReservationRepository;
import com.avanzada.alojamientos.repositories.UserRepository;
import com.avanzada.alojamientos.services.NotificationService;
import com.avanzada.alojamientos.services.EmailNotificationService;
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
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    // Constantes para evitar duplicación
    private static final String DATE_FORMAT = "dd/MM/yyyy";
    private static final String DATETIME_FORMAT = "dd/MM/yyyy HH:mm";
    private static final String NO_REASON_SPECIFIED = "No especificado";
    private static final String RESERVATION_CANCELLED_SUBJECT = "Reserva cancelada - ";
    private static final String TEAM_SIGNATURE = "\n\nSaludos,\nEquipo de Alojamientos";

    private final ReservationRepository reservationRepository;
    private final AccommodationRepository accommodationRepository;
    private final UserRepository userRepository;
    private final ReservationMapper mapper;
    private final NotificationService notificationService;
    private final EmailNotificationService emailNotificationService;

    @Override
    public ReservationDTO create(Long userId, CreateReservationDTO dto) {


        log.info("Creating reservation for user {} in accommodation {}",
                userId, dto.accommodationId());

        // 1. Validar que quien reserva es un USER (no un HOST)
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // 2. Validar que el alojamiento existe y no está eliminado
        AccommodationEntity accommodation = accommodationRepository.findById(dto.accommodationId())
                .orElseThrow(() -> new AccommodationNotFoundException("Accommodation not found"));

        if (Boolean.TRUE.equals(accommodation.getSoftDeleted())) {
            throw new ReservationAvailabilityException("Cannot reserve a deleted accommodation");
        }

        // 3. Validar que el usuario no está reservando su propio alojamiento
        if (accommodation.getHost().getId().equals(userId)) {
            throw new ReservationPermissionException("You cannot reserve your own accommodation");
        }

        // 4. Validar fechas no sean pasadas
        if (dto.startDate().isBefore(LocalDate.now())) {
            throw new ReservationValidationException("Cannot reserve past dates");
        }

        // 5. Validar mínimo 1 noche
        long nights = ChronoUnit.DAYS.between(dto.startDate(), dto.endDate());
        if (nights < 1) {
            throw new ReservationValidationException("Minimum stay is 1 night");
        }

        // 6. Validar capacidad máxima de huéspedes
        Integer guests = dto.guests(); // Asumiendo que agregaste este campo al DTO
        if (guests > accommodation.getMaxGuests()) {
            throw new ReservationValidationException(
                    String.format("Maximum capacity is %d guests", accommodation.getMaxGuests())
            );
        }

        // 7. Validar disponibilidad (no solapamiento)
        if (!isAvailable(dto.accommodationId(), dto.startDate(), dto.endDate(), guests)) {
            throw new ReservationAvailabilityException("Accommodation not available for selected dates");
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
        try {
            String hostNotificationTitle = "Nueva reserva recibida";
            String hostNotificationBody = String.format(
                "Has recibido una nueva reserva de %s para el alojamiento '%s' desde %s hasta %s. Total: $%.2f",
                user.getName(),
                accommodation.getTitle(),
                dto.startDate().format(DateTimeFormatter.ofPattern(DATE_FORMAT)),
                dto.endDate().format(DateTimeFormatter.ofPattern(DATE_FORMAT)),
                totalPrice
            );
            String hostMetadata = String.format("reservation_id:%d,type:new_reservation", saved.getId());

            notificationService.create(
                accommodation.getHost().getId(),
                hostNotificationTitle,
                hostNotificationBody,
                hostMetadata
            );

            // Enviar email al anfitrión
            EmailDTO hostEmailDTO = new EmailDTO(
                "Nueva reserva en tu alojamiento - " + accommodation.getTitle(),
                String.format("""
                    Hola %s,

                    Has recibido una nueva reserva:

                    Huésped: %s
                    Alojamiento: %s
                    Fechas: %s - %s
                    Huéspedes: %d
                    Noches: %d
                    Total: $%.2f

                    Por favor, revisa y confirma la reserva en tu panel de control.%s""",
                    accommodation.getHost().getName(),
                    user.getName(),
                    accommodation.getTitle(),
                    dto.startDate().format(DateTimeFormatter.ofPattern(DATE_FORMAT)),
                    dto.endDate().format(DateTimeFormatter.ofPattern(DATE_FORMAT)),
                    guests,
                    nights,
                    totalPrice,
                    TEAM_SIGNATURE
                ),accommodation.getHost().getEmail()
            );
            emailNotificationService.sendMail(hostEmailDTO);
        } catch (Exception e) {
            log.error("Error sending notification to host for reservation {}", saved.getId(), e);
        }

        // 11. Enviar email de confirmación al usuario
        try {
            String guestNotificationTitle = "Reserva creada exitosamente";
            String guestNotificationBody = String.format(
                "Tu reserva para '%s' ha sido creada. Estado: Pendiente de confirmación. Total: $%.2f",
                accommodation.getTitle(),
                totalPrice
            );
            String guestMetadata = String.format("reservation_id:%d,type:new_reservation", saved.getId());

            notificationService.create(
                userId,
                guestNotificationTitle,
                guestNotificationBody,
                guestMetadata
            );

            // Enviar email de confirmación al usuario
            EmailDTO guestEmailDTO = new EmailDTO(
                "Confirmación de reserva - " + accommodation.getTitle(),
                String.format("""
                    Hola %s,

                    Tu reserva ha sido creada exitosamente:

                    Alojamiento: %s
                    Fechas: %s - %s
                    Huéspedes: %d
                    Noches: %d
                    Total: $%.2f
                    Estado: Pendiente de confirmación

                    El anfitrión revisará tu solicitud y te notificaremos cuando sea confirmada.%s""",
                    user.getName(),
                    accommodation.getTitle(),
                    dto.startDate().format(DateTimeFormatter.ofPattern(DATE_FORMAT)),
                    dto.endDate().format(DateTimeFormatter.ofPattern(DATE_FORMAT)),
                    guests,
                    nights,
                    totalPrice,
                    TEAM_SIGNATURE
                ),user.getEmail()
            );
            emailNotificationService.sendMail(guestEmailDTO);
        } catch (Exception e) {
            log.error("Error sending confirmation email to user for reservation {}", saved.getId(), e);
        }

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
                .orElseThrow(() -> new AccommodationNotFoundException("Accommodation not found"));

        long nights = ChronoUnit.DAYS.between(start, end);
        return accommodation.getPricePerNight().multiply(BigDecimal.valueOf(nights));
    }

    @Transactional
    @Override
    public void cancel(Long reservationId, Long cancelledByUserId, String motivo) {

        log.info("Cancelling reservation {} by user {}", reservationId, cancelledByUserId);

        ReservationEntity reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        UserEntity cancelledBy = userRepository.findById(cancelledByUserId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Validar que quien cancela es el dueño de la reserva o el host
        boolean isGuest = reservation.getUser().getId().equals(cancelledBy.getId());
        boolean isHost = reservation.getAccommodation().getHost().getId().equals(cancelledBy.getId());

        if (!isGuest && !isHost) {
            throw new ReservationPermissionException("You don't have permission to cancel this reservation");
        }

        // Validar estado actual
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new ReservationStateException("Reservation is already cancelled");
        }

        if (reservation.getStatus() == ReservationStatus.COMPLETED) {
            throw new ReservationStateException("Cannot cancel a completed reservation");
        }

        // Si es el usuario (huésped), validar 48 horas antes
        if (isGuest) {
            LocalDateTime checkInDateTime = reservation.getStartDate().atStartOfDay();
            LocalDateTime now = LocalDateTime.now();
            long hoursUntilCheckIn = ChronoUnit.HOURS.between(now, checkInDateTime);

            if (hoursUntilCheckIn < 48) {
                throw new ReservationValidationException(
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

        // Notificar a la otra parte (huésped o anfitrión)
        notifyReservationCancellation(reservation, cancelledBy, motivo, isGuest, reservationId);

        log.info("Reservation {} cancelled by {} ({})", reservationId,
                cancelledBy.getName(),
                isGuest ? "guest" : "host");
    }

    private void notifyReservationCancellation(ReservationEntity reservation, UserEntity cancelledBy,
                                             String motivo, boolean isGuest, Long reservationId) {
        try {
            String reasonText = motivo != null ? motivo : NO_REASON_SPECIFIED;

            if (isGuest) {
                notifyHostOfCancellation(reservation, cancelledBy, reasonText, reservationId);
            } else {
                notifyGuestOfCancellation(reservation, reasonText, reservationId);
            }
        } catch (Exception e) {
            log.error("Error sending cancellation notification for reservation {}", reservationId, e);
        }
    }

    private void notifyHostOfCancellation(ReservationEntity reservation, UserEntity cancelledBy,
                                        String reasonText, Long reservationId) throws Exception {
        String hostNotificationTitle = "Reserva cancelada por el huésped";
        String hostNotificationBody = String.format(
            "La reserva #%d en '%s' ha sido cancelada por el huésped %s. Motivo: %s",
            reservationId,
            reservation.getAccommodation().getTitle(),
            cancelledBy.getName(),
            reasonText
        );
        String hostMetadata = String.format("reservation_id:%d,type:cancelled_reservation", reservationId);

        notificationService.create(
            reservation.getAccommodation().getHost().getId(),
            hostNotificationTitle,
            hostNotificationBody,
            hostMetadata
        );

        EmailDTO hostEmailDTO = new EmailDTO(
            reservation.getAccommodation().getHost().getEmail(),
            RESERVATION_CANCELLED_SUBJECT + reservation.getAccommodation().getTitle(),
            String.format("""
                Hola %s,

                La reserva #%d en tu alojamiento '%s' ha sido cancelada por el huésped.

                Detalles:
                Huésped: %s
                Fechas: %s - %s
                Motivo: %s
                Fecha de cancelación: %s%s""",
                reservation.getAccommodation().getHost().getName(),
                reservationId,
                reservation.getAccommodation().getTitle(),
                cancelledBy.getName(),
                reservation.getStartDate().format(DateTimeFormatter.ofPattern(DATE_FORMAT)),
                reservation.getEndDate().format(DateTimeFormatter.ofPattern(DATE_FORMAT)),
                reasonText,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATETIME_FORMAT)),
                TEAM_SIGNATURE
            )
        );
        emailNotificationService.sendMail(hostEmailDTO);
    }

    private void notifyGuestOfCancellation(ReservationEntity reservation, String reasonText,
                                         Long reservationId) throws Exception {
        String guestNotificationTitle = "Reserva cancelada por el anfitrión";
        String guestNotificationBody = String.format(
            "Tu reserva #%d en '%s' ha sido cancelada por el anfitrión. Motivo: %s",
            reservationId,
            reservation.getAccommodation().getTitle(),
            reasonText
        );
        String guestMetadata = String.format("reservation_id:%d,type:cancelled_reservation", reservationId);

        notificationService.create(
            reservation.getUser().getId(),
            guestNotificationTitle,
            guestNotificationBody,
            guestMetadata
        );

        EmailDTO guestEmailDTO = new EmailDTO(
            RESERVATION_CANCELLED_SUBJECT + reservation.getAccommodation().getTitle(),
            String.format("""
                Hola %s,

                Lamentamos informarte que tu reserva #%d ha sido cancelada por el anfitrión.

                Detalles:
                Alojamiento: %s
                Fechas: %s - %s
                Motivo: %s
                Fecha de cancelación: %s

                Nos disculpamos por los inconvenientes causados.%s""",
                reservation.getUser().getName(),
                reservationId,
                reservation.getAccommodation().getTitle(),
                reservation.getStartDate().format(DateTimeFormatter.ofPattern(DATE_FORMAT)),
                reservation.getEndDate().format(DateTimeFormatter.ofPattern(DATE_FORMAT)),
                reasonText,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATETIME_FORMAT)),
                TEAM_SIGNATURE
            ),reservation.getUser().getEmail()
        );
        emailNotificationService.sendMail(guestEmailDTO);
    }

    @Transactional
    @Override
    public void updateStatus(Long reservationId, ReservationStatus status, Long hostId) {

        log.info("Host {} updating reservation {} to status {}",
                hostId, reservationId, status);

        ReservationEntity reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        // Validar que quien actualiza es el dueño del alojamiento
        if (!reservation.getAccommodation().getHost().getId().equals(hostId)) {
            throw new ReservationPermissionException("You don't have permission to update this reservation");
        }

        // Validar transiciones de estado válidas
        ReservationStatus currentStatus = reservation.getStatus();

        if (currentStatus == ReservationStatus.CANCELLED) {
            throw new ReservationStateException("Cannot change status of cancelled reservation");
        }

        if (currentStatus == ReservationStatus.COMPLETED) {
            throw new ReservationStateException("Cannot change status of completed reservation");
        }

        // Si está en PENDING, solo puede pasar a CONFIRMED o CANCELLED
        if (currentStatus == ReservationStatus.PENDING && status != ReservationStatus.CONFIRMED && status != ReservationStatus.CANCELLED) {
                throw new ReservationValidationException("Can only confirm or cancel pending reservations");
            }

        reservation.setStatus(status);
        reservationRepository.save(reservation);

        // Notificar al huésped sobre el cambio de estado
        try {
            sendStatusUpdateNotification(reservation, status, reservationId);
        } catch (Exception e) {
            log.error("Error sending status update notification for reservation {}", reservationId, e);
        }

        log.info("Reservation {} status updated to {}", reservationId, status);
    }

    private void sendStatusUpdateNotification(ReservationEntity reservation, ReservationStatus status,
                                            Long reservationId) throws Exception {
        String notificationTitle;
        String notificationBody;
        String emailSubject;
        String emailBody;

        switch (status) {
            case CONFIRMED -> {
                notificationTitle = "Reserva confirmada";
                notificationBody = String.format(
                    "Tu reserva #%d en '%s' ha sido confirmada por el anfitrión.",
                    reservationId,
                    reservation.getAccommodation().getTitle()
                );
                emailSubject = "Reserva confirmada - " + reservation.getAccommodation().getTitle();
                emailBody = String.format("""
                    Hola %s,

                    ¡Excelentes noticias! Tu reserva ha sido confirmada.

                    Detalles de la reserva:
                    Alojamiento: %s
                    Fechas: %s - %s
                    Total: $%.2f

                    ¡Esperamos que disfrutes tu estadía!%s""",
                    reservation.getUser().getName(),
                    reservation.getAccommodation().getTitle(),
                    reservation.getStartDate().format(DateTimeFormatter.ofPattern(DATE_FORMAT)),
                    reservation.getEndDate().format(DateTimeFormatter.ofPattern(DATE_FORMAT)),
                    reservation.getTotalPrice(),
                    TEAM_SIGNATURE
                );
            }
            case CANCELLED -> {
                notificationTitle = "Reserva cancelada";
                notificationBody = String.format(
                    "Tu reserva #%d en '%s' ha sido cancelada por el anfitrión.",
                    reservationId,
                    reservation.getAccommodation().getTitle()
                );
                emailSubject = RESERVATION_CANCELLED_SUBJECT + reservation.getAccommodation().getTitle();
                emailBody = String.format("""
                    Hola %s,

                    Lamentamos informarte que tu reserva ha sido cancelada por el anfitrión.

                    Detalles:
                    Alojamiento: %s
                    Fechas: %s - %s

                    Nos disculpamos por los inconvenientes.%s""",
                    reservation.getUser().getName(),
                    reservation.getAccommodation().getTitle(),
                    reservation.getStartDate().format(DateTimeFormatter.ofPattern(DATE_FORMAT)),
                    reservation.getEndDate().format(DateTimeFormatter.ofPattern(DATE_FORMAT)),
                    TEAM_SIGNATURE
                );
            }
            case COMPLETED -> {
                notificationTitle = "Estancia completada";
                notificationBody = String.format(
                    "Tu estancia en '%s' ha sido marcada como completada. ¡Esperamos que hayas disfrutado!",
                    reservation.getAccommodation().getTitle()
                );
                emailSubject = "Estancia completada - " + reservation.getAccommodation().getTitle();
                emailBody = String.format("""
                    Hola %s,

                    Tu estancia en '%s' ha sido completada.

                    ¡Esperamos que hayas disfrutado tu estadía!
                    No olvides dejar un comentario sobre tu experiencia.%s""",
                    reservation.getUser().getName(),
                    reservation.getAccommodation().getTitle(),
                    TEAM_SIGNATURE
                );
            }
            default -> {
                // No enviar notificación para otros estados
                return;
            }
        }

        String metadata = String.format("reservation_id:%d,type:%s", reservationId, status.name().toLowerCase());

        notificationService.create(
            reservation.getUser().getId(),
            notificationTitle,
            notificationBody,
            metadata
        );

        EmailDTO emailDTO = new EmailDTO(
            emailSubject,
            emailBody,
                reservation.getUser().getEmail()
        );
        emailNotificationService.sendMail(emailDTO);
    }
}
