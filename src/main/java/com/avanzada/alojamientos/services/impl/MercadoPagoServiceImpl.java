package com.avanzada.alojamientos.services.impl;

import com.avanzada.alojamientos.DTO.model.PaymentMethod;
import com.avanzada.alojamientos.DTO.model.PaymentStatus;
import com.avanzada.alojamientos.DTO.other.PaymentDTO;
import com.avanzada.alojamientos.entities.ReservationEntity;
import com.avanzada.alojamientos.exceptions.ReservationNotFoundException;
import com.avanzada.alojamientos.repositories.ReservationRepository;
import com.avanzada.alojamientos.services.MercadoPagoService;
import com.avanzada.alojamientos.services.PaymentService;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class MercadoPagoServiceImpl implements MercadoPagoService {

    private final ReservationRepository reservationRepository;
    private final PaymentService paymentService;

    @Value("${mercadopago.access.token}")
    private String mercadoPagoToken;

    // URLs de retorno - IMPORTANTE: Cambiar en producción
    private static final String SUCCESS_URL = "http://localhost:4200/reservation?status=approved";
    private static final String FAILURE_URL = "http://localhost:4200/reservation?status=failure";
    private static final String PENDING_URL = "http://localhost:4200/reservation?status=pending";

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
            String accommodationTitle = reservation.getAccommodation() != null
                    ? reservation.getAccommodation().getTitle()
                    : "Alojamiento";

            // 3️⃣ Crear el ítem con TODOS los campos requeridos
            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .id("res_" + reservation.getId())
                    .title(accommodationTitle)
                    .description("Reserva del " + reservation.getStartDate() + " al " + reservation.getEndDate())
                    .pictureUrl("https://via.placeholder.com/500x300.png?text=Alojamiento")
                    .categoryId("travel")
                    .quantity(1)
                    .currencyId("COP")
                    .unitPrice(totalAmount)
                    .build();

            List<PreferenceItemRequest> items = new ArrayList<>();
            items.add(item);

            log.info("📦 Item creado: title={}, price={}", accommodationTitle, totalAmount);

            // 4️⃣ Configurar URLs de retorno (OBLIGATORIAS)
            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(SUCCESS_URL)
                    .failure(FAILURE_URL)
                    .pending(PENDING_URL)
                    .build();

            log.info("🔗 URLs configuradas - Success: {}, Failure: {}, Pending: {}",
                    SUCCESS_URL, FAILURE_URL, PENDING_URL);

            // 5️⃣ Configurar datos del pagador
            PreferencePayerRequest payer = PreferencePayerRequest.builder()
                    .name(reservation.getUser().getName())
                    .email(reservation.getUser().getEmail())
                    .build();

            log.info("👤 Pagador configurado: name={}, email={}",
                    reservation.getUser().getName(), reservation.getUser().getEmail());

            // 6️⃣ Crear la preferencia - SIN autoReturn para evitar problemas
            PreferenceRequest.PreferenceRequestBuilder requestBuilder = PreferenceRequest.builder()
                    .items(items)
                    .backUrls(backUrls)
                    .payer(payer)
                    .externalReference("reservation_" + reservationId)
                    .statementDescriptor("ALOJAMIENTO");

            // Solo agregar autoReturn si las URLs están correctamente configuradas
            // Por ahora lo comentamos para que funcione
            // requestBuilder.autoReturn("approved");

            PreferenceRequest preferenceRequest = requestBuilder.build();

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
}