package com.analistas.luzclaritaweb.web.config;

import java.io.IOException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class WebSecurityConfig {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository; // Para OAuth2

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
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
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF protection is enabled by default, no need to explicitly enable it
                // .csrf(csrf -> csrf.enable()) el token esta activado por defecto
                .authorizeHttpRequests((requests) -> requests
                        // .requestMatchers("/", "/home", "/img/**", "/js/**", "/css/**", "/assets/**",
                        // "/consultas/**",
                        // "/inicioSesion/**", "/registro/**", "/receta-clasica/**",
                        // "/receta-especial/**", "/proveedor/listado", "/proveedor/**",
                        // "/inventario/listado", "/inventario/**", "/inventario/form",
                        // "/inventario/editar", "/inventario/guardar", "/inventario/borrar",
                        // "/productos/**", "/success/**")
                        // .permitAll()

                        // Rutas públicas (acceso para todos)
                        .requestMatchers("/", "/home", "/img/**", "/js/**", "/css/**", "/assets/**",
                                "/consultas/**", "/inicioSesion/**", "/registro/**",
                                "/receta-clasica/**", "/receta-especial/**", "/productos/**")
                        .permitAll()

                        // Rutas para administradores (ROL 2)
                        // .requestMatchers("/admin/**", "/inventario/**", "/productos/**", "/proveedor/**")
                        // .hasAnyRole("ADMIN")

                        
                        // Rutas para administradores (ROL 3)
                        // .requestMatchers("/admin/**", "/inventario/**", "/productos/**", "/proveedor/**")
                        // .hasAnyRole("PROGRAMADOR")

                        .requestMatchers("/admin/**", "/inventario/**", "/productos/**", "/proveedor/**").hasAnyRole("ADMIN", "PROGRAMADOR") // Sin ROLE_

                        .requestMatchers("/", "/home", "/img/**", "/js/**", "/css/**", "/assets/**", "/index",
                                "/consultas/**", "/inicioSesion/**", "/registro/**",
                                "/receta-clasica/**", "/receta-especial/**", "/productos/**")
                        .hasAnyRole("CLIENTE")

                        .anyRequest().authenticated())
                .formLogin((form) -> form
                        .loginPage("/inicioSesion/login")
                        .defaultSuccessUrl("/home", true)
                        .failureUrl("/inicioSesion/login?error=true")
                        .usernameParameter("emailOrUser")
                        .passwordParameter("password")
                        .permitAll())
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/inicioSesion/login") // Página de inicio de sesión personalizada
                        .defaultSuccessUrl("/home", true) // Redirigir después del éxito
                        .failureUrl("/inicioSesion/login?error=true") // Redirigir en caso de error
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(this.oidcUserService()) // Para Google
                                .userService(this.oauth2UserService()) // Para Facebook
                        ))
                .logout(logout -> logout
                        .logoutSuccessUrl("/").permitAll()
                        .logoutSuccessHandler(new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository)) // Cerrar
                                                                                                                         // sesión
                                                                                                                         // en
                                                                                                                         // OAuth2
                )
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
        return new com.analistas.luzclaritaweb.model.service.CustomOidcUserService();
    }

    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
        return new com.analistas.luzclaritaweb.model.service.CustomOAuth2UserService();
    }

    @Autowired
    public void configGlobal(AuthenticationManagerBuilder builder) throws Exception {

        builder
        .jdbcAuthentication()
        .dataSource(dataSource)
        .passwordEncoder(passwordEncoder())
        .usersByUsernameQuery("select nomb_usu, clave, activo from usuarios where nomb_usu = ?")
        .authoritiesByUsernameQuery(
            "SELECT u.nomb_usu, " +
            "CASE " +
            "   WHEN p.nombre LIKE '%PROGRAMADOR%' THEN 'ROLE_PROGRAMADOR' " +
            "   WHEN p.nombre LIKE '%ADMIN%' THEN 'ROLE_ADMIN' " +
            "   WHEN p.nombre LIKE '%OPERADOR%' THEN 'ROLE_OPERADOR' " +
            "   WHEN p.nombre LIKE '%CLIENTE%' THEN 'ROLE_CLIENTE' " +
            "   WHEN p.nombre LIKE '%UNLOGUED%' THEN 'ROLE_UNLOGGED' " +
            "END as authority " +
            "FROM permisos p INNER JOIN usuarios u ON u.id_permiso = p.id WHERE u.nomb_usu = ?"
        );

        builder
                .jdbcAuthentication()
                .dataSource(dataSource)
                .passwordEncoder(passwordEncoder())
                .usersByUsernameQuery("select email, clave, activo from usuarios where email = ?")
                .authoritiesByUsernameQuery(
                        "select u.email, " +
                                "CASE " +
                                "   WHEN p.nombre LIKE '%PROGRAMADOR%' THEN 'ROLE_PROGRAMADOR' " +
                                "   WHEN p.nombre LIKE '%ADMIN%' THEN 'ROLE_ADMIN' " +
                                "   WHEN p.nombre LIKE '%OPERADOR%' THEN 'ROLE_OPERADOR' " +
                                "   WHEN p.nombre LIKE '%CLIENTE%' THEN 'ROLE_CLIENTE' " +
                                "   WHEN p.nombre LIKE '%UNLOGUED%' THEN 'ROLE_UNLOGGED' " +
                                "END " +
                                "from permisos p inner join usuarios u on u.id_permiso = p.id where u.email = ?");

    }

    @Component
    public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

        private final MessageSource messageSource;

        public CustomAuthenticationFailureHandler(MessageSource messageSource) {
            this.messageSource = messageSource;
            setDefaultFailureUrl("/inicioSesion/login?error"); // Redirige con parámetro error
        }

        @Override
        public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                AuthenticationException exception)
                throws IOException, ServletException {
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
                AccessDeniedException accessDeniedException)
                throws IOException, ServletException {
            String errorMessage = messageSource.getMessage("error.accessDenied", null, LocaleContextHolder.getLocale());
            request.getSession().setAttribute("accessDeniedMessage", errorMessage);
            response.sendRedirect(request.getContextPath() + "/accessDenied"); // Redirige a /accessDenied
        }
    }
}