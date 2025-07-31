package com.analistas.luzclaritaweb.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// Archivo de configuracion para poder usar BCryptPasswordEncoder
// Permite manejar tanto $2y$ (PHP) como $2a$ (Java) para la codificación de contraseñas
// Esto es necesario porque PHP usa $2y$ y Java usa $2a$ para BCrypt
// Esta clase define un bean de PasswordEncoder personalizado que maneja ambas variantes
@Configuration
public class PasswordEncoderConfig {

    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new CustomPasswordEncoder();
    }
}

// Clase personalizada para manejar ambas variantes de BCrypt
// Esta clase implementa PasswordEncoder y utiliza BCryptPasswordEncoder internamente
// Formatea las contraseñas para que sean compatibles con ambas versiones de BCrypt
// Permite que las contraseñas codificadas en PHP funcionen correctamente en Java
class CustomPasswordEncoder implements PasswordEncoder {

    private final BCryptPasswordEncoder bCryptEncoder = new BCryptPasswordEncoder();

    @Override
    public String encode(CharSequence rawPassword) {
        return bCryptEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {

        // Manejar todas la variantes de BCrypt
        if (encodedPassword.startsWith("$2")) {
            // Normalizar a formato $2a$ para compatibilidad
            String normalizedEncodedPassword = encodedPassword
                    .replaceFirst("\\$2y\\$", "\\$2a\\$")
                    .replaceFirst("\\$2b\\$", "\\$2a\\$");

            return bCryptEncoder.matches(rawPassword, normalizedEncodedPassword);
        }
        return bCryptEncoder.matches(rawPassword, encodedPassword);
    }
}
