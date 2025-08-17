package com.analistas.luzclaritaweb.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import com.mercadopago.MercadoPago;
import com.mercadopago.exceptions.MPConfException;

@Configuration
public class MercadoPagoConfig implements CommandLineRunner {

    @Value("${mercadopago.access_token}")
    private String accessToken;

    @Override
    @SuppressWarnings("CallToPrintStackTrace")
    public void run(String... args) throws Exception {
        try {
            MercadoPago.SDK.setAccessToken(accessToken);
            System.out.println("Mercado Pago SDK configured successfully via CommandLineRunner.");
        } catch (MPConfException e) {
            System.err.println("Error configuring Mercado Pago SDK: " + e.getMessage());
            e.printStackTrace();
        }
    }
}