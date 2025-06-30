package com.analistas.luzclaritaweb.model.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

//Servicio para cargar detalles del usuario desde la base de datos
//Sirve para autenticar usuarios con nombre de usuario o email
@Service("userDetailsService") // Nombre del bean requerido
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        
        
        String sql = "SELECT nomb_usu, clave, activo FROM usuarios WHERE nomb_usu = ? OR email = ?";
        
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            String nombUsu = rs.getString("nomb_usu");
            String clave = rs.getString("clave");
            boolean activo = rs.getBoolean("activo");

            // Consulta de roles
            // Cambia el nombre de la tabla y las columnas según tu esquema de base de datos
            String authoritiesSql = "SELECT p.nombre FROM permisos p " +
                        "INNER JOIN usuarios u ON u.id_permiso = p.id " +
                        "WHERE u.nomb_usu = ? OR u.email = ?";
            String role = jdbcTemplate.queryForObject(authoritiesSql, String.class, usernameOrEmail, usernameOrEmail);

            return User.builder()
                    .username(nombUsu)
                    .password(clave)
                    .disabled(!activo)
                    .authorities(role != null ? role : "ROLE_CLIENTE") // Usa authorities en lugar de roles 

                    .build();
        }, usernameOrEmail, usernameOrEmail);
    }
} //Te quedaste en reemplazar todos los roles