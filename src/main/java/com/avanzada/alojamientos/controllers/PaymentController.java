package com.avanzada.alojamientos.controllers;

import com.avanzada.alojamientos.DTO.PaymentDTO;
import com.avanzada.alojamientos.services.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public PaymentDTO register(@RequestBody @Valid PaymentDTO dto) {
        return paymentService.register(dto);
    }

    @GetMapping("/{id}")
    public Optional<PaymentDTO> findById(@PathVariable String id) {
        return paymentService.findById(id);
    }

    @GetMapping("/reservation/{reservationId}")
    public List<PaymentDTO> findByReservation(@PathVariable String reservationId) {
        return paymentService.findByReservation(reservationId);
    }
}
