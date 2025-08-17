package com.analistas.luzclaritaweb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import com.analistas.luzclaritaweb.model.service.interfaces.IFileStorageService;


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
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

}
