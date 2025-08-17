package com.analistas.luzclaritaweb.web.config;

import java.io.IOException;

import javax.sql.DataSource;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.stereotype.Component;

import com.analistas.luzclaritaweb.web.config.security.CustomAuthenticationSuccessHandler;
import com.analistas.luzclaritaweb.web.config.security.CustomUniversalLogoutSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class WebSecurityConfig {

    private final DataSource dataSource;
    private final MessageSource messageSource;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final UserDetailsService userDetailsService;

    public WebSecurityConfig(DataSource dataSource, MessageSource messageSource,
            ClientRegistrationRepository clientRegistrationRepository, UserDetailsService userDetailsService) {
        this.dataSource = dataSource;
        this.messageSource = messageSource;
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public AuthenticationFailureHandler customAuthenticationFailureHandler() {
        return new CustomAuthenticationFailureHandler(messageSource);
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new CustomAccessDeniedHandler(messageSource);
    }

    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl tokenRepo = new JdbcTokenRepositoryImpl();
        tokenRepo.setDataSource(dataSource);
        return tokenRepo;
    }

    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return new CustomAuthenticationSuccessHandler();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        CustomUniversalLogoutSuccessHandler logoutSuccessHandler = new CustomUniversalLogoutSuccessHandler(
                clientRegistrationRepository);
        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                .authorizeHttpRequests((requests) -> requests

                        // Public routes
                        .requestMatchers(
                                "/","/static/**" , "/home", "/img/**", "/js/**", "/css/**", "/font/**", "/uploads/**", "/assets/**",
                                "/consultas/**", "/inicioSesion/**", "/registro/**",
                                "/recetas/cards**", "/recetas/detalle**", "/productos/**", "/productos/listado",
                                "/accessDenied", "/api/usuario/actual", "/api/usuario/verificar", "/cursos/listado2")
                        .permitAll()

                        //URL de carrito
                        .requestMatchers("/api/carrito/**").hasAnyAuthority("ROLE_CLIENTE", "ROLE_ADMIN")
                        // Profile route
                        .requestMatchers("/perfil/**").authenticated()

                        // Admin routes
                        .requestMatchers("/inventario/ajax/crear-rapido").hasAnyAuthority("ROLE_ADMIN")
                        .requestMatchers("/admin/**", "/inventario/**", "/proveedor/**", "/caja/**", "/cursos/**")
                        .hasAnyAuthority("ROLE_ADMIN")

                        // Prductos - permitir la lectura para clientes y escritura para Admin
                        .requestMatchers(HttpMethod.GET, "/productos/listado").hasAuthority("ROLE_CLIENTE")
                        .requestMatchers("/productos/**").hasAnyAuthority("ROLE_ADMIN")

                        // Rutas de MercadoPago - acceso para clientes y admin
                        .requestMatchers("/createAndRedirect", "/success", "/failure", "/pending")
                        .hasAnyAuthority("ROLE_CLIENTE", "ROLE_ADMIN")

                        // Client routes
                        .requestMatchers("/api/carrito/**")
                        .hasAnyAuthority("ROLE_CLIENTE", "ROLE_ADMIN")
                        .anyRequest().authenticated())
                .formLogin((form) -> form
                        .loginPage("/inicioSesion/login")
                        .loginProcessingUrl("/login") // URL de procesamiento del login
                        .successHandler(customAuthenticationSuccessHandler())
                        .failureUrl("/inicioSesion/login?error")
                        .usernameParameter("emailOrUser")
                        .passwordParameter("password")
                        .permitAll())
                .userDetailsService(userDetailsService) // Inyectar el servicio corregido
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/inicioSesion/login")
                        .successHandler(customAuthenticationSuccessHandler())
                        .failureUrl("/inicioSesion/login?error")
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(this.oidcUserService())
                                .userService(this.oauth2UserService())))
                .logout(logout -> logout
                        .logoutSuccessHandler(logoutSuccessHandler)
                        .permitAll())
                .rememberMe(rememberMe -> rememberMe
                        .key(System.getenv("SPRING_SECURITY_REMEMBER_ME_KEY"))
                        .tokenRepository(persistentTokenRepository())
                        .tokenValiditySeconds(1209600)
                        .userDetailsService(userDetailsService))
                .exceptionHandling((exceptions) -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            request.getSession().setAttribute("error",
                                    "Error de autenticación: " + authException.getMessage());
                            response.sendRedirect("/inicioSesion/login");
                        })
                        .accessDeniedHandler(accessDeniedHandler()));
        return http.build();
    }

    @Bean
    public OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        return new com.analistas.luzclaritaweb.web.config.security.CustomOidcUserService();
    }

    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
        return new com.analistas.luzclaritaweb.web.config.security.CustomOAuth2UserService();
    }

    // Handlers internos...
    @Component
    public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
        private final MessageSource messageSource;

        public CustomAuthenticationFailureHandler(MessageSource messageSource) {
            this.messageSource = messageSource;
            setDefaultFailureUrl("/inicioSesion/login?error");
        }

        @Override
        public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                AuthenticationException exception) throws IOException, ServletException {
            String errorMessage = messageSource.getMessage("error.auth", null, LocaleContextHolder.getLocale());
            request.getSession().setAttribute("errorMessage", errorMessage);
            super.onAuthenticationFailure(request, response, exception);
        }
    }

    @Component
    public class CustomAccessDeniedHandler implements AccessDeniedHandler {
        private final MessageSource messageSource;

        public CustomAccessDeniedHandler(MessageSource messageSource) {
            this.messageSource = messageSource;
        }

        @Override
        public void handle(HttpServletRequest request, HttpServletResponse response,
                AccessDeniedException accessDeniedException) throws IOException, ServletException {
            String errorMessage = messageSource.getMessage("error.accessDenied", null, LocaleContextHolder.getLocale());
            request.getSession().setAttribute("accessDeniedMessage", errorMessage);
            response.sendRedirect(request.getContextPath() + "/accessDenied");
        }
    }

}
