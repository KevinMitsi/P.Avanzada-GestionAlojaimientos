package com.avanzada.alojamientos.controllers;

import com.avanzada.alojamientos.DTO.other.PaymentDTO;
import com.avanzada.alojamientos.services.MercadoPagoService;
import com.avanzada.alojamientos.services.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mercadopago")
@RequiredArgsConstructor
@Slf4j
public class MercadoPagoController {

    private final MercadoPagoService mercadoPagoService;
    private final PaymentService paymentService;

    @PostMapping("/create-preference/{reservationId}")
    public ResponseEntity<?> createPreference(@PathVariable Long reservationId) {
        try {
            log.info("🎯 Iniciando creación de preferencia para reserva: {}", reservationId);

            String preferenceUrl = mercadoPagoService.createPreference(reservationId);

            if (preferenceUrl == null || preferenceUrl.isBlank()) {
                log.error("❌ URL de preferencia vacía");
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(createErrorResponse("Error al crear la preferencia: respuesta vacía"));
            }

            log.info("✅ Preferencia creada exitosamente: {}", preferenceUrl);

            // Retornar la URL en formato texto plano
            return ResponseEntity.ok(preferenceUrl);

        } catch (IllegalArgumentException e) {
            log.error("❌ Argumento inválido: {}", e.getMessage());
            return ResponseEntity
                    .badRequest()
                    .body(createErrorResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("❌ Error al crear la preferencia: {}", e.getMessage(), e);

            // Extraer mensaje de error más específico
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("Api error")) {
                errorMessage = "Error de MercadoPago. Verifica tu token de acceso y la configuración.";
            }

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(errorMessage));
        }
    }

    @PostMapping("/confirm/{reservationId}")
    public ResponseEntity<?> confirmPaymentAfterSuccess(
            @PathVariable Long reservationId,
            @RequestParam(name = "status", required = false) String status
    ) {
        try {
            log.info("🎯 Confirmando pago para reserva: {}, status: {}", reservationId, status);

            if (!"COMPLETED".equalsIgnoreCase(status) && !"approved".equalsIgnoreCase(status)) {
                log.warn("⚠️ Estado de pago no válido: {}", status);
                return ResponseEntity
                        .badRequest()
                        .body(createErrorResponse("Pago no aprobado. Estado: " + status));
            }

            List<PaymentDTO> payments = paymentService.findByReservation(reservationId);

            if (payments == null || payments.isEmpty()) {
                log.error("❌ No se encontró pago para la reserva: {}", reservationId);
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("No se encontró pago asociado a la reserva " + reservationId));
            }

            PaymentDTO payment = payments.get(0);
            log.info("💳 Pago encontrado: ID={}, Status={}", payment.id(), payment.status());

            PaymentDTO confirmedPayment = paymentService.confirmPayment(payment.id());
            log.info("✅ Pago confirmado exitosamente: ID={}", confirmedPayment.id());

            return ResponseEntity.ok(createSuccessResponse("Pago confirmado y actualizado correctamente"));

        } catch (RuntimeException e) {
            log.error("❌ Error al confirmar el pago: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error al confirmar el pago: " + e.getMessage()));
        }
    }

    /**
     * Webhook para recibir notificaciones de MercadoPago (IPN)
     * MercadoPago enviará notificaciones aquí cuando cambie el estado del pago
     */
    @PostMapping("/webhook")
    public ResponseEntity<?> recibirWebhook(@RequestBody Map<String, Object> payload) {
        try {
            log.info("🔔 Webhook recibido de Mercado Pago: {}", payload);

            // Validación básica
            if (payload == null || !payload.containsKey("type")) {
                log.warn("⚠ Webhook inválido o sin 'type'");
                return ResponseEntity.badRequest().build();
            }

            String type = (String) payload.get("type");

            if (!"payment".equalsIgnoreCase(type)) {
                log.info("📦 Tipo de notificación ignorado: {}", type);
                return ResponseEntity.ok("Tipo no manejado");
            }

            // Extraer ID de pago
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            if (data == null || !data.containsKey("id")) {
                log.warn("⚠ Payload sin ID de pago");
                return ResponseEntity.badRequest().build();
            }

            Long paymentId = Long.parseLong(data.get("id").toString());
            log.info("💳 ID de pago recibido: {}", paymentId);

            // Consultar pago con MercadoPagoService
            mercadoPagoService.procesarPagoWebhook(paymentId);

            return ResponseEntity.ok("Notificación procesada correctamente");

        } catch (Exception e) {
            log.error("❌ Error procesando webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }


    // Métodos auxiliares para crear respuestas consistentes

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        response.put("success", "false");
        return response;
    }

    private Map<String, String> createSuccessResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        response.put("success", "true");
        return response;
    }
}