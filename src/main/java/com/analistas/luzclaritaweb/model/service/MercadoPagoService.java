/*package com.analistas.luzclaritaweb.model.service;

import com.mercadopago.MP;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MercadoPagoService {
    @Value("${mercadopago.access.token}")
    private String accessToken;

    @Bean
    public MP mercadoPago() {
        return new MP(accessToken);
    }
} 
*/