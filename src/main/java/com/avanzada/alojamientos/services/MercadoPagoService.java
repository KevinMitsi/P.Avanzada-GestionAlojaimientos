package com.avanzada.alojamientos.services;

import com.avanzada.alojamientos.DTO.other.MercadoPagoPreferenceDTO;

import java.util.Map;

public interface MercadoPagoService {
    String createPreference(Long reservationId) throws Exception;

    void procesarPagoWebhook(Long paymentId)throws Exception;
}
