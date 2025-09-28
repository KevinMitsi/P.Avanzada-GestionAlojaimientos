package com.avanzada.alojamientos.services;

import com.avanzada.alojamientos.DTO.reservation.CreateReservationDTO;

import com.avanzada.alojamientos.DTO.reservation.ReservationDTO;
import com.avanzada.alojamientos.DTO.reservation.ReservationSearchCriteria;
import com.avanzada.alojamientos.DTO.model.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface ReservationService {
    ReservationDTO create(Long userId, CreateReservationDTO dto);
    Optional<ReservationDTO> findById(Long reservationId);

    // Listados
    Page<ReservationDTO> findByUser(Long userId, Pageable pageable);
    Page<ReservationDTO> findByAccommodation(Long accommodationId, Pageable pageable);

    // Filtros por estado, fechas, usuario/host
    Page<ReservationDTO> searchReservations(ReservationSearchCriteria criteria, Pageable pageable);

    // Validaciones
    boolean isAvailable(Long accommodationId, LocalDate start, LocalDate end, int guests);
    BigDecimal calculatePrice(Long accommodationId, LocalDate start, LocalDate end, int guests);

    // Cancelaciones
    void cancel(Long reservationId, Long cancelledByUserId, String motivo);

    // Flujo manual (opcional): aprobar o rechazar
    void updateStatus(Long reservationId, ReservationStatus status, Long hostId);
}
