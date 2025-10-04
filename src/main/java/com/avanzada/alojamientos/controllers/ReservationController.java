package com.avanzada.alojamientos.controllers;

import com.avanzada.alojamientos.DTO.reservation.CreateReservationDTO;

import com.avanzada.alojamientos.DTO.reservation.ReservationDTO;
import com.avanzada.alojamientos.DTO.reservation.ReservationSearchCriteria;
import com.avanzada.alojamientos.DTO.model.ReservationStatus;
import com.avanzada.alojamientos.services.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping("/{userId}")
    public ReservationDTO create(@PathVariable Long userId,
                                 @RequestBody @Valid CreateReservationDTO dto) {
        return reservationService.create(userId, dto);
    }

    @GetMapping("/{reservationId}")
    public Optional<ReservationDTO> findById(@PathVariable Long reservationId) {
        return reservationService.findById(reservationId);
    }

    @GetMapping("/user/{userId}")
    public Page<ReservationDTO> findByUser(@PathVariable Long userId,  @ParameterObject Pageable pageable) {
        return reservationService.findByUser(userId, pageable);
    }

    @GetMapping("/accommodation/{accommodationId}")
    public Page<ReservationDTO> findByAccommodation(@PathVariable Long accommodationId, @ParameterObject Pageable pageable) {
        return reservationService.findByAccommodation(accommodationId, pageable);
    }

    @PostMapping("/search")
    public Page<ReservationDTO> search(@RequestBody ReservationSearchCriteria criteria, @ParameterObject Pageable pageable) {
        return reservationService.searchReservations(criteria, pageable);
    }

    @GetMapping("/availability")
    public boolean isAvailable(@RequestParam Long accommodationId,
                               @RequestParam LocalDate start,
                               @RequestParam LocalDate end,
                               @RequestParam int guests) {
        return reservationService.isAvailable(accommodationId, start, end, guests);
    }

    @GetMapping("/price")
    public BigDecimal calculatePrice(@RequestParam Long accommodationId,
                                     @RequestParam LocalDate start,
                                     @RequestParam LocalDate end,
                                     @RequestParam int guests) {
        return reservationService.calculatePrice(accommodationId, start, end, guests);
    }

    @PutMapping("/{reservationId}/cancel")
    public void cancel(@PathVariable Long reservationId,
                       @RequestParam Long cancelledByUserId,
                       @RequestParam String motivo) {
        reservationService.cancel(reservationId, cancelledByUserId, motivo);
    }

    @PutMapping("/{reservationId}/status")
    public void updateStatus(@PathVariable Long reservationId,
                             @RequestParam ReservationStatus status,
                             @RequestParam Long hostId) {
        reservationService.updateStatus(reservationId, status, hostId);
    }
}
