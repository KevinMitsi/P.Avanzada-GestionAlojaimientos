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
import com.mercadopago.resources.preference.Preference;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class MercadoPagoServiceImpl implements MercadoPagoService {

    private final ReservationRepository reservationRepository;
    private final PaymentService paymentService;

    @Value("${mercadopago.access.token}")
    private String mercadoPagoToken;


    @Transactional
    @Override
    public String createPreference(Long reservationId) throws Exception {
        // 1️⃣ Configurar Mercado Pago SDK
        MercadoPagoConfig.setAccessToken(mercadoPagoToken);

        // 2️⃣ Buscar la reserva
        ReservationEntity reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("Reserva no encontrada con id=" + reservationId));

        BigDecimal totalAmount = reservation.getTotalPrice();

        // 3️⃣ Crear el ítem (producto o servicio)
        PreferenceItemRequest item = PreferenceItemRequest.builder()
                .id("reservation_" + reservation.getId())
                .title("Reserva alojamiento: " + reservation.getAccommodation().getTitle())
                .description("De " + reservation.getStartDate() + " al " + reservation.getEndDate())
                .pictureUrl("https://via.placeholder.com/300x200.png?text=Alojamiento") // 👈 evita error por imagen vacía
                .categoryId("accommodation") // 👈 usa un categoryId válido
                .quantity(1)
                .currencyId("COP")
                .unitPrice(reservation.getTotalPrice())
                .build();


        List<PreferenceItemRequest> items = new ArrayList<>();
        items.add(item);
        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(items).build();

        PreferenceClient client = new PreferenceClient();
        Preference preference = client.create(preferenceRequest);





        // 6️ Registrar el pago en tu base de datos con estado PENDING
        PaymentDTO paymentDTO = new PaymentDTO(
                null,
                reservationId,
                totalAmount,
                PaymentMethod.PAYPAL, // tu enum de métodos
                PaymentStatus.PENDING,
                null
        );
        paymentService.register(paymentDTO);

        // 7️⃣ Devolver la URL de pago al frontend
        return preference.getInitPoint();
    }
}
