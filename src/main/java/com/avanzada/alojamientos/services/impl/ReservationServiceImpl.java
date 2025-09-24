package com.avanzada.alojamientos.services.impl;

import com.avanzada.alojamientos.DTO.CreateReservationDTO;
import com.avanzada.alojamientos.DTO.ReservationDTO;
import com.avanzada.alojamientos.DTO.ReservationSearchCriteriaDTO;
import com.avanzada.alojamientos.DTO.model.ReservationStatus;
import com.avanzada.alojamientos.services.ReservationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Service
@Slf4j
public class ReservationServiceImpl implements ReservationService {
    @Override
    public ReservationDTO create(Long userId, CreateReservationDTO dto) {
        return null;
    }

    @Override
    public Optional<ReservationDTO> findById(Long reservationId) {
        return Optional.empty();
    }

    @Override
    public Page<ReservationDTO> findByUser(Long userId, Pageable pageable) {
        return null;
    }

    @Override
    public Page<ReservationDTO> findByAccommodation(Long accommodationId, Pageable pageable) {
        return null;
    }

    @Override
    public Page<ReservationDTO> searchReservations(ReservationSearchCriteriaDTO criteria, Pageable pageable) {
        return null;
    }

    @Override
    public boolean isAvailable(Long accommodationId, LocalDate start, LocalDate end, int guests) {
        return false;
    }

    @Override
    public BigDecimal calculatePrice(Long accommodationId, LocalDate start, LocalDate end, int guests) {
        return null;
    }

    @Override
    public void cancel(Long reservationId, Long cancelledByUserId, String motivo) {

    }

    @Override
    public void updateStatus(Long reservationId, ReservationStatus status, Long hostId) {

    }
}
