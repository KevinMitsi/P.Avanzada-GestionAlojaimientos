package com.avanzada.alojamientos.services.impl;

import com.avanzada.alojamientos.DTO.other.PaymentDTO;
import com.avanzada.alojamientos.services.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    @Override
    public PaymentDTO register(PaymentDTO dto) {
        return null;
    }

    @Override
    public Optional<PaymentDTO> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public List<PaymentDTO> findByReservation(Long reservationId) {
        return List.of();
    }
}
