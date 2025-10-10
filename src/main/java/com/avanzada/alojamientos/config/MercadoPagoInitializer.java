package com.avanzada.alojamientos.config;


import com.mercadopago.MercadoPagoConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class MercadoPagoInitializer {

    @Value("${mercadopago.access.token}")
    private String accessToken;

    @PostConstruct
    public void init() {
        System.out.println("✅ MercadoPago token cargado: " + accessToken.substring(0, 10) + "...");
        MercadoPagoConfig.setAccessToken(accessToken);
    }

}

