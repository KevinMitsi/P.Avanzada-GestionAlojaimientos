package com.avanzada.alojamientos.controllers;

import com.avanzada.alojamientos.DTO.other.PaymentDTO;
import com.avanzada.alojamientos.services.MercadoPagoService;
import com.avanzada.alojamientos.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mercadopago")
@RequiredArgsConstructor
public class MercadoPagoController {

    private final MercadoPagoService mercadoPagoService;
    private final PaymentService paymentService;

    @PostMapping("/create-preference/{reservationId}")
    public ResponseEntity<String> createPreference(@PathVariable Long reservationId) {
        try {
            String preferenceUrl = mercadoPagoService.createPreference(reservationId);

            if (preferenceUrl == null || preferenceUrl.isBlank()) {
                return ResponseEntity.badRequest().body("Error al crear la preferencia: respuesta vacía");
            }

            return ResponseEntity.ok(preferenceUrl);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al crear la preferencia: " + e.getMessage());
        }
    }

    @PostMapping("/confirm/{reservationId}")
    public ResponseEntity<String> confirmPaymentAfterSuccess(
            @PathVariable Long reservationId,
            @RequestParam(name = "status", required = false) String status) {

        if (!"COMPLETED".equalsIgnoreCase(status)) {
            return ResponseEntity.badRequest().body("Pago no aprobado");
        }

        List<PaymentDTO> payments = paymentService.findByReservation(reservationId);

        if (payments == null || payments.isEmpty()) {
            return ResponseEntity.badRequest().body("No se encontró pago asociado a la reserva " + reservationId);
        }

        try {
            PaymentDTO payment = payments.get(0);
            paymentService.confirmPayment(payment.id());
            return ResponseEntity.ok("Pago confirmado y actualizado correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error al confirmar el pago: " + e.getMessage());
        }
    }
}
