package com.avanzada.alojamientos.services.impl;

import com.avanzada.alojamientos.DTO.model.PaymentMethod;
import com.avanzada.alojamientos.DTO.model.PaymentStatus;
import com.avanzada.alojamientos.DTO.model.ReservationStatus;
import com.avanzada.alojamientos.DTO.other.PaymentDTO;
import com.avanzada.alojamientos.entities.AccommodationEntity;
import com.avanzada.alojamientos.entities.PaymentEntity;
import com.avanzada.alojamientos.entities.ReservationEntity;
import com.avanzada.alojamientos.exceptions.AccommodationNotFoundException;
import com.avanzada.alojamientos.exceptions.ReservationNotFoundException;
import com.avanzada.alojamientos.repositories.AccommodationRepository;
import com.avanzada.alojamientos.repositories.PaymentRepository;
import com.avanzada.alojamientos.repositories.ReservationRepository;
import com.avanzada.alojamientos.services.MercadoPagoService;
import com.avanzada.alojamientos.services.PaymentService;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class MercadoPagoServiceImpl implements MercadoPagoService {

    private final ReservationRepository reservationRepository;
    private final PaymentService paymentService;
    private final AccommodationRepository accommodationRepository;
    private final PaymentRepository paymentRepository;

    @Value("${mercadopago.access.token}")
    private String mercadoPagoToken;

    // URLs de retorno - IMPORTANTE: Cambiar en producción


    @Transactional
    @Override
    public String createPreference(Long reservationId) throws Exception {
        try {
            // 1️⃣ Configurar Mercado Pago SDK
            MercadoPagoConfig.setAccessToken(mercadoPagoToken);
            log.info("🔑 MercadoPago configurado correctamente");

            // 2️⃣ Buscar la reserva
            ReservationEntity reservation = reservationRepository.findById(reservationId)
                    .orElseThrow(() -> new ReservationNotFoundException(
                            "Reserva no encontrada con id=" + reservationId));

            log.info("📋 Reserva encontrada: ID={}, Total={}",
                    reservationId, reservation.getTotalPrice());

            // Validar que el precio sea mayor a 0
            if (reservation.getTotalPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("El precio de la reserva debe ser mayor a 0");
            }

            BigDecimal totalAmount = reservation.getTotalPrice();
            AccommodationEntity accommodation=accommodationRepository.findById(reservation.getAccommodation().getId()).orElseThrow(() -> new AccommodationNotFoundException(
                    "alojamiento no encontrada con id=" + reservation.getAccommodation().getId()));

            // 3️⃣ Crear el ítem con TODOS los campos requeridos
            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .id("res_" + reservation.getId())
                    .title(accommodation.getTitle())
                    .description("Reserva del " + reservation.getStartDate() + " al " + reservation.getEndDate())
                    .pictureUrl("https://via.placeholder.com/500x300.png?text=Alojamiento")
                    .categoryId("travel")
                    .quantity(1)
                    .currencyId("COP")
                    .unitPrice(totalAmount)
                    .build();

            List<PreferenceItemRequest> items = new ArrayList<>();
            items.add(item);

            log.info("📦 Item creado: title={}, price={}", accommodation.getTitle(), totalAmount);

            String baseFrontUrl = "https://ten-readers-cross.loca.lt";

            String successUrl = baseFrontUrl + "/reservation?status=approved&reservationId=" + reservationId;
            String failureUrl = baseFrontUrl + "/reservation?status=failure&reservationId=" + reservationId;
            String pendingUrl = baseFrontUrl + "/reservation?status=pending&reservationId=" + reservationId;


            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(successUrl)
                    .failure(failureUrl)
                    .pending(pendingUrl)
                    .build();


            log.info("🔗 URLs configuradas - Success: {}, Failure: {}, Pending: {}",
                    successUrl, failureUrl, pendingUrl);

            // 5️⃣ Configurar datos del pagador
            PreferencePayerRequest payer = PreferencePayerRequest.builder()
                    .name(reservation.getUser().getName())
                    .email(reservation.getUser().getEmail())
                    .build();

            log.info("👤 Pagador configurado: name={}, email={}",
                    reservation.getUser().getName(), reservation.getUser().getEmail());

            String webhookUrl = "https://public-turtles-boil.loca.lt/api/mercadopago/webhook";


            // 6️⃣ Crear la preferencia - SIN autoReturn para evitar problemas
            PreferenceRequest.PreferenceRequestBuilder requestBuilder = PreferenceRequest.builder()
                    .items(items)
                    .backUrls(backUrls)
                    .notificationUrl(webhookUrl)
                    .payer(payer)
                    .externalReference("reservation_" + reservationId)
                    .statementDescriptor("ALOJAMIENTO");



            PreferenceRequest preferenceRequest = requestBuilder
                    .autoReturn("approved") // 👈 esta línea permite el redireccionamiento automático
                    .build();

            log.info("🚀 Creando preferencia en MercadoPago...");

            // 7️⃣ Crear la preferencia
            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            log.info("✅ Preferencia creada exitosamente: ID={}", preference.getId());

            // 8️⃣ Registrar el pago en la base de datos con estado PENDING
            PaymentDTO paymentDTO = new PaymentDTO(
                    null,
                    reservationId,
                    totalAmount,
                    PaymentMethod.PAYPAL, // Considera crear PaymentMethod.MERCADOPAGO
                    PaymentStatus.PENDING,
                    null
            );
            paymentService.register(paymentDTO);

            log.info("💾 Pago registrado en BD con estado PENDING");

            // 9️⃣ Devolver la URL de pago
            String initPoint = preference.getInitPoint();
            log.info("🔗 URL de pago generada: {}", initPoint);

            return initPoint;

        } catch (MPApiException e) {
            // Error específico de la API de MercadoPago
            log.error("❌ Error de API de MercadoPago: Status={}, Message={}",
                    e.getStatusCode(), e.getMessage());

            // Intentar obtener más detalles del error
            String errorDetails = "Sin detalles adicionales";
            try {
                if (e.getApiResponse() != null && e.getApiResponse().getContent() != null) {
                    errorDetails = e.getApiResponse().getContent();
                }
            } catch (Exception ex) {
                log.warn("No se pudieron obtener detalles del error");
            }

            log.error("❌ Detalles del error: {}", errorDetails);
            throw new Exception("Error de MercadoPago: " + errorDetails, e);

        } catch (MPException e) {
            // Error general de MercadoPago
            log.error("❌ Error general de MercadoPago: {}", e.getMessage(), e);
            throw new Exception("Error al crear preferencia: " + e.getMessage(), e);

        } catch (Exception e) {
            // Cualquier otro error
            log.error("❌ Error inesperado al crear preferencia: {}", e.getMessage(), e);
            throw new Exception("Error al procesar el pago: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void procesarPagoWebhook(Long paymentId) throws Exception {
        // Configura tu token de acceso
        MercadoPagoConfig.setAccessToken(mercadoPagoToken);

        // Nuevo cliente oficial
        PaymentClient client = new PaymentClient();
        Payment mpPayment = client.get(paymentId);

        if (mpPayment == null) {
            log.warn("⚠ No se encontró información del pago {}", paymentId);
            return;
        }

        log.info("📄 Detalle del pago desde MP: status={}, external_reference={}",
                mpPayment.getStatus(), mpPayment.getExternalReference());

        // Buscar la reserva asociada
        String externalRef = mpPayment.getExternalReference();
        Long reservationId = null;

// Verificamos formato "reservation_38"
        if (externalRef != null && externalRef.startsWith("reservation_")) {
            reservationId = Long.parseLong(externalRef.replace("reservation_", ""));
        } else {
            reservationId = Long.parseLong(externalRef);
        }
        Optional<ReservationEntity> optionalReservation = reservationRepository.findById(reservationId);

        if (optionalReservation.isEmpty()) {
            log.error("❌ No se encontró la reserva con ID: {}", reservationId);
            return;
        }

        ReservationEntity reservation = optionalReservation.get();

        // Buscar el pago local
        Optional<PaymentEntity> optionalPayment = paymentRepository.findByReservationId(reservationId)
                .stream().findFirst();

        if (optionalPayment.isEmpty()) {
            log.error("❌ No se encontró el pago asociado a la reserva {}", reservationId);
            return;
        }

        PaymentEntity localPayment = optionalPayment.get();

        // Actualizar estado
        switch (mpPayment.getStatus()) {
            case "approved" -> {
                localPayment.setStatus(PaymentStatus.COMPLETED);
                localPayment.setPaidAt(LocalDateTime.now());
                reservation.setStatus(ReservationStatus.CONFIRMED);
                log.info("✅ Pago aprobado, reserva confirmada");
            }
            case "rejected" -> {
                localPayment.setStatus(PaymentStatus.FAILED);
                reservation.setStatus(ReservationStatus.CANCELLED);
                log.info("❌ Pago rechazado, reserva cancelada");
            }
            default -> {
                localPayment.setStatus(PaymentStatus.PENDING);
                log.info("⌛ Pago pendiente");
            }
        }

        paymentRepository.save(localPayment);
        reservationRepository.save(reservation);
    }
}


