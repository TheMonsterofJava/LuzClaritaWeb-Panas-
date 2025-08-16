package com.analistas.luzclaritaweb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import com.analistas.luzclaritaweb.model.service.interfaces.IFileStorageService;
import com.mercadopago.MercadoPago;
import com.mercadopago.exceptions.MPConfException;


@SpringBootApplication
public class LuzClaritaWebApplication implements CommandLineRunner {

    @Autowired
    IFileStorageService fileStorageService;

    public static void main(String[] args) {
        System.setProperty("spring.config.additional-location", "optional:file:./.env.properties");
        SpringApplication.run(LuzClaritaWebApplication.class, args);

    }

    @Override
    @SuppressWarnings("CallToPrintStackTrace")
	public void run(String... args) throws Exception {

        //Iniciamos el almacenamiento de archivos
        fileStorageService.init();


        // Configuración del SDK de Mercado Pago
        try {
            MercadoPago.SDK.setAccessToken("APP_USR-1477346058343548-032009-60ac3523a2a29e97e32f939ec16a947f-210899344");
            System.out.println("Mercado Pago SDK configured successfully.");
        } catch (MPConfException e) {
            System.err.println("Error configuring Mercado Pago SDK: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

}
