package com.analistas.luzclaritaweb.web.config;

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
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.io.IOException;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class WebSecurityConfig {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private MessageSource messageSource;

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
                .csrf(csrf -> csrf.disable())
                // .authorizeHttpRequests((request) -> request
                // .requestMatchers("/", "/home", "/img/**", "/js/**", "/css/**", "/font/**",
                // "/assets/**", "/cdn-cgi/**",
                // "/consejos/**", "/consultas/guardar", "/consultas/consulta",
                // "/inicioSesion/**",
                // "/layout/**", "/productos/**", "/receta-clasica/**", "/registro",
                // "/registro/**",
                // "/receta-especial/**", "/index", "/templates/**", "/font/**",
                // "/src/main/resources/templates/admin/dashboard-1.html",
                // "/assets/img/illustrations/profiles/**")
                // .permitAll()
                // .anyRequest().authenticated())
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/", "/home", "/img/**", "/js/**", "/css/**", "/assets/**", "/consultas/**",
                                "/inicioSesion/**", "/registro/**", "/receta-clasica/**", "/receta-especial/**")
                        .permitAll()
                        .anyRequest().authenticated())
                .formLogin((form) -> form
                        // .loginPage("/inicioSesion/login")
                        // .defaultSuccessUrl("/home", true)
                        // .failureHandler(customAuthenticationFailureHandler())
                        .loginPage("/inicioSesion/login") // Ruta al formulario de login
                        .defaultSuccessUrl("/home") // Redirigir tras login exitoso
                        .failureUrl("/inicioSesion/login?error=true") // Redirigir tras fallo
                        .usernameParameter("emailOrUser") // Nombre del parámetro del formulario
                        .passwordParameter("password") // Nombre del parámetro del formulario
                        .permitAll())
                .logout(logout -> logout.permitAll())
                .exceptionHandling((exceptions) -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            request.getSession().setAttribute("error",
                                    "Error de autenticación: " + authException.getMessage());
                            response.sendRedirect("/inicioSesion/login");
                        })
                        .accessDeniedHandler(accessDeniedHandler()));

        return http.build();
    }

    @Autowired
    public void configGlobal(AuthenticationManagerBuilder builder) throws Exception {
        // Autenticacion para Usuarios(Clientes tambien) con nombre de usuario
        builder
                .jdbcAuthentication()
                .dataSource(dataSource)
                .passwordEncoder(passwordEncoder())
                .usersByUsernameQuery("select nomb_usu, clave, activo from usuarios where nomb_usu = ?")
                .authoritiesByUsernameQuery(
                        "select u.nomb_usu, p.nombre from permisos p inner join usuarios u on u.id_permiso = p.id where u.nomb_usu = ?");

        // Autenticacion para Usuarios(Clientes tambien) con email
        builder
                .jdbcAuthentication()
                .dataSource(dataSource)
                .passwordEncoder(passwordEncoder())
                .usersByUsernameQuery("select email, clave, activo from usuarios where email = ?")
                .authoritiesByUsernameQuery(
                        "select u.email, p.nombre from permisos p inner join usuarios u on u.id_permiso = p.id where u.email = ?");

    }

    @Component
    public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

        private final MessageSource messageSource;

        public CustomAuthenticationFailureHandler(MessageSource messageSource) {
            this.messageSource = messageSource;
            setDefaultFailureUrl("/inicioSesion/login?error"); // Redirige con parámetro error
        }

        @Override
        public void onAuthenticationFailure(jakarta.servlet.http.HttpServletRequest request,
                jakarta.servlet.http.HttpServletResponse response, AuthenticationException exception)
                throws IOException, jakarta.servlet.ServletException {
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
        public void handle(jakarta.servlet.http.HttpServletRequest request,
                jakarta.servlet.http.HttpServletResponse response, AccessDeniedException accessDeniedException)
                throws IOException, jakarta.servlet.ServletException {
            String errorMessage = messageSource.getMessage("error.accessDenied", null, LocaleContextHolder.getLocale());
            request.getSession().setAttribute("accessDeniedMessage", errorMessage);
            response.sendRedirect(request.getContextPath() + "/accessDenied"); // Redirige a /accessDenied
        }
    }
}