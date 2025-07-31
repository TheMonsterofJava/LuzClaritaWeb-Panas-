package com.analistas.luzclaritaweb.web.config.security;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.analistas.luzclaritaweb.model.domain.Usuario;
import com.analistas.luzclaritaweb.model.repository.IUsuarioRepository;

//Este codigo Utiliza JPA/Hibernate para cargar detalles del usuario desde la base de datos
//Permite autenticar usuarios con nombre de usuario o email

//Servicio para cargar detalles del usuario
// Implementa UserDetailsService para la autenticación de usuarios en Spring Security
@Service("customUserDetailsService")
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    // Logger para registrar eventos
    // Utilizado para depuración y seguimiento de errores
    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final IUsuarioRepository usuariosRepository;

    public CustomUserDetailsService(IUsuarioRepository usuariosRepository) {
        this.usuariosRepository = usuariosRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String emailOrUser) throws UsernameNotFoundException {

        logger.info("Buscando usuario por email o nombre de usuario: {}", emailOrUser);

        Usuario usuario = usuariosRepository.findByEmailOrNombUsu(emailOrUser)
                .orElseThrow(() -> {
                    logger.error("Usuario no encontrado: {}", emailOrUser);
                    return new UsernameNotFoundException("Usuario no encontrado");
                });
        logger.info("Usuario encontrado: {}", usuario.getEmail());
        logger.info("Contraseña en BD: {}", usuario.getClave());
        logger.info("Roles: {}", usuario.getPermiso().getNombre());

        // Verificar si el usuario está activo
        if (!usuario.isActivo()) {
            throw new UsernameNotFoundException("Usuario inactivo: " + emailOrUser);
        }

        // Convertir permiso a autoridad Spring Security
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (usuario.getPermiso() != null && usuario.getPermiso().getNombre() != null) {

            authorities.add(new SimpleGrantedAuthority(usuario.getPermiso().getNombre()));
        } else {
            // Rol por defecto si no tiene permiso asignado
            authorities.add(new SimpleGrantedAuthority("ROLE_CLIENTE"));
        }

        // Crear y devolver el objeto UserDetails con la información del usuario

        return new CustomUserDetails(
                usuario.getEmail(),
                usuario.getClave(),
                usuario.isActivo(),
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                authorities,
                usuario.getId(),
                usuario.getNomb_usu() // Guargar el nombre de usuario
        );
    }
}