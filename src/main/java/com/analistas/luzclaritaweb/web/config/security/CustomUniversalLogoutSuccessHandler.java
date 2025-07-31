package com.analistas.luzclaritaweb.web.config.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

//Clase que maneja el logout de los usuarios
//Se encarga de redirigir a la página de logout correspondiente según el tipo de usuario
//En este caso, si el usuario es OIDC, redirige a la página de logout de OIDC
//Si el usuario es tradicional o de Facebook, redirige a la página de logout tradicional
public class CustomUniversalLogoutSuccessHandler implements LogoutSuccessHandler {

    private final OidcClientInitiatedLogoutSuccessHandler oidcHandler;

    public CustomUniversalLogoutSuccessHandler(ClientRegistrationRepository clientRegistrationRepository) {
        
        this.oidcHandler = new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
        this.oidcHandler.setPostLogoutRedirectUri("{baseUrl}");

    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        boolean isOidc = authentication != null && authentication.getPrincipal() instanceof org.springframework.security.oauth2.core.oidc.user.OidcUser;
        if (isOidc) {
            oidcHandler.onLogoutSuccess(request, response, authentication);
        } else {
            // Tradicional o Facebook: redirige a home con parámetro
            response.sendRedirect(request.getContextPath() + "/");
        }
    }

}
