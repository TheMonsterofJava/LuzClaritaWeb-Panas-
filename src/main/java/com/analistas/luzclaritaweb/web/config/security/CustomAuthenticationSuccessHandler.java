package com.analistas.luzclaritaweb.web.config.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

//Clase personalizada para manejar el éxito de la autenticación y redirigir con parámetros adicionales.
//Esta clase extiende SavedRequestAwareAuthenticationSuccessHandler para aprovechar la funcionalidad de redirección basada en solicitudes guardadas.
//SavedRequestAwareAuthenticationSuccessHandler sirve para redirigir al usuario a la URL que intentaba acceder antes de ser autenticado, o a una URL por defecto si no hay una solicitud guardada.
//Ejemplo: Si un usuario intenta acceder a una página protegida y es redirigido al login, después de autenticarse, será redirigido a la página original o a una URL por defecto.
@Component("autenticacionExitoHandler")
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    public CustomAuthenticationSuccessHandler() {
        // Define la URL por defecto si no hay una solicitud guardada (por ejemplo, si el usuario va directamente a /login).
        setDefaultTargetUrl("/home");
        // Le decimos al handler que NO siempre use la URL por defecto, para que pueda usar la solicitud guardada si existe.
        setAlwaysUseDefaultTargetUrl(false);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        
        // Obtenemos la URL de destino que Spring Security habría usado.
        // Esta será la URL guardada (si el usuario fue interceptado) o la defaultTargetUrl.
        String targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        // Añadimos nuestro parámetro 'sync=true' a la URL de destino.
        // Esto es crucial para que el frontend sepa que debe sincronizar el carrito.
        String finalUrl = UriComponentsBuilder.fromUriString(targetUrl)
                                               .queryParam("sync", "true")
                                               .build().toUriString();

        // Usamos la estrategia de redirección para enviar al usuario a la URL final.
        getRedirectStrategy().sendRedirect(request, response, finalUrl);
    }
}