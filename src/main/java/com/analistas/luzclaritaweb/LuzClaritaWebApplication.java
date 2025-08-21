package com.analistas.luzclaritaweb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import com.analistas.luzclaritaweb.model.service.interfaces.IUploadFileService;
import com.mercadopago.MercadoPago;


@SpringBootApplication
public class LuzClaritaWebApplication implements CommandLineRunner {

    public static void main(String[] args) {
        System.setProperty("spring.config.additional-location", "optional:file:./.env.properties");
        SpringApplication.run(LuzClaritaWebApplication.class, args);

    }

    @Autowired
    IUploadFileService uploadFileService;

    @Override
	public void run(String... args) throws Exception {

        uploadFileService.deleteAll();
        uploadFileService.init();

		//Credenciales de prueba (las pruebas sólo funcionan en un navegador que NO ESTÉ logueado en MP):
		// MercadoPago.SDK.setAccessToken(System.getenv("APP_USR-1758590432023103-030711-b15a87044d281636f9e29a2ab28fd8f3-144433383"));
        // MercadoPago.SDK.setClientId("1758590432023103");
        // MercadoPago.SDK.setClientSecret("fUggj57yyTmzQd4DOgdccx07hym6WkYh");

        //Credenciales de producción Franco:
        MercadoPago.SDK.setAccessToken(System.getenv("APP_USR-1477346058343548-032009-60ac3523a2a29e97e32f939ec16a947f-210899344"));
        MercadoPago.SDK.setClientId("1477346058343548");
        MercadoPago.SDK.setClientSecret("cNElGoSaOL3oTVvUPT1uoajyAGNMF54Z");
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

}
