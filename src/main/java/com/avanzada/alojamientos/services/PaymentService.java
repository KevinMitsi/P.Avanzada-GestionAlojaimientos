package com.avanzada.alojamientos.services;

import com.avanzada.alojamientos.DTO.PaymentDTO;

import java.util.List;
import java.util.Optional;

public interface PaymentService {
    PaymentDTO register(PaymentDTO dto);
    Optional<PaymentDTO> findById(Long id);
    List<PaymentDTO> findByReservation(Long reservationId);
}
