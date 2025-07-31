package com.analistas.luzclaritaweb.web.config.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

//Componente que maneja el éxito de la autenticación
// Redirige al usuario a la página de inicio de sesión con parámetros específicos
@Component("autenticacionExitoHandler")
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        
        // URL base de redirección
        String redirectUrl = "/inicioSesion/login";
        
        // Detectar si viene de un intento de checkout/pago
        String fromCheckout = request.getParameter("fromCheckout");
        boolean isFromCheckout = "true".equals(fromCheckout);
        
        // Construir parámetros de la URL
        StringBuilder params = new StringBuilder();
        params.append("?success=").append("Inicio+de+sesión+exitoso");
        
        // Si viene de checkout, agregar syncCart=true
        if (isFromCheckout) {
            params.append("&syncCart=true");
        }
        
        // URL final
        redirectUrl += params.toString();
        
        System.out.println("CustomAuthenticationSuccessHandler: Redirigiendo a " + redirectUrl);
        System.out.println("From checkout: " + isFromCheckout);
        
        response.sendRedirect(redirectUrl);
    }
}