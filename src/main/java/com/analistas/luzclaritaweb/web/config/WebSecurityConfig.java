package com.analistas.luzclaritaweb.web.config;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class WebSecurityConfig {

    @Autowired
    private DataSource dataSource;

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests((request) -> request
                        .requestMatchers("/", "/home", "/img/**", "/js/**", "/css/**", "/font/**",
                                "/consejos/**", "/consultas/guardar", "/consultas/consulta", "/inicioSesion/**",
                                "/layout/**", "/productos/**", "/receta-clasica/**", "/registro", "/registro/**",
                                "/receta-especial/**", "/index")
                        .permitAll()
                        .anyRequest().authenticated())
                .formLogin((form) -> form
                        .loginPage("/inicioSesion/login")
                        .defaultSuccessUrl("/home", true)
                        .failureUrl("/inicioSesion/login?error=true")
                        .permitAll())
                .logout(logout -> logout.permitAll())
                .exceptionHandling((exceptions) -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            request.getSession().setAttribute("error",
                                    "Error de autenticación: " + authException.getMessage());
                            response.sendRedirect("/inicioSesion/login");
                        }));

        return http.build();
    }

    @Autowired
    public void configGlobal(AuthenticationManagerBuilder builder) throws Exception {
        builder
                .jdbcAuthentication()
                .dataSource(dataSource)
                .passwordEncoder(passwordEncoder())
                .usersByUsernameQuery("select nombre, clave, activo from usuarios where nombre = ?")
                .authoritiesByUsernameQuery("select u.nombre, p.nombre from permisos p "
                        + "inner join usuarios u on u.id_permiso = p.id"
                        + " where u.nombre = ?");
    }
}

// @Configuration
// @EnableWebSecurity
// @EnableMethodSecurity(
// prePostEnabled = true,
// securedEnabled = true
// )
// public class WebSecurityConfig {
//
// @Autowired
// DataSource dataSource; //Todo lo de la BD lo almacena aca
//
// @Bean
// public SecurityFilterChain securityFilterChain(HttpSecurity http) throws
// Exception {
// http.authorizeHttpRequests((request) -> request
// .requestMatchers(" /", "/home", "/img/**", "/js/**", "/css/**", "/font/**",
// "/consejos/**", "/consultas/**", "/inicioSesion/**",
// "/layout/**", "/productos/**", "/receta-clasica/**",
// "/receta-especial/**", "/index").permitAll()
// .anyRequest().authenticated()
// )
// .formLogin((form) -> form
// .loginPage("/inicioSesion/login")
// .defaultSuccessUrl("/consultas/consulta", true) //Define la pagina en donde
// entrara el usuario cuando inicie una sesion exitosa...
// .loginProcessingUrl("/inicioSesion/login")
// .permitAll()
// )
// .logout(logout -> logout.permitAll());
// return http.build();
// }
//
// @Autowired
// public void configGlobal(AuthenticationManagerBuilder builder) throws
// Exception {
//
// builder
// .jdbcAuthentication()
// .dataSource(dataSource)
// .usersByUsernameQuery("select nombre, clave, activo from usuarios where
// nombre = ?")
// .authoritiesByUsernameQuery("select u.nombre, p.nombre from permisos p "
// + "inner join usuarios u on u.id_permiso = p.id"
// + " where u.nombre = ?");
//
// }
//
// }
