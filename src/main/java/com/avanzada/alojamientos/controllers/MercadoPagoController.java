package com.avanzada.alojamientos.controllers;

import com.avanzada.alojamientos.DTO.other.MercadoPagoPreferenceDTO;
import com.avanzada.alojamientos.DTO.other.PaymentDTO;
import com.avanzada.alojamientos.services.MercadoPagoService;
import com.avanzada.alojamientos.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
            return ResponseEntity.ok(preferenceUrl);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al crear la preferencia: " + e.getMessage());
        }
    }
    @PostMapping("/confirm/{reservationId}")
    public ResponseEntity<String> confirmPaymentAfterSuccess(
            @PathVariable Long reservationId,
            @RequestParam(name = "status", required = false) String status) {

        if (!"approved".equalsIgnoreCase(status)) {
            return ResponseEntity.badRequest().body("Pago no aprobado");
        }

        // Buscar el pago pendiente por la reserva
        List<PaymentDTO> payments = paymentService.findByReservation(reservationId);
        if (payments.isEmpty()) {
            return ResponseEntity.badRequest().body("No se encontró pago asociado a la reserva " + reservationId);
        }

        PaymentDTO payment = payments.get(0);
        paymentService.confirmPayment(payment.id());

        return ResponseEntity.ok("✅ Pago confirmado y actualizado correctamente en la base de datos");
    }

}
