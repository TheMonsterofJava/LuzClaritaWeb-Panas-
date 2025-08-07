package com.analistas.luzclaritaweb.web.config.security;

import com.analistas.luzclaritaweb.model.domain.Usuario;
import com.analistas.luzclaritaweb.model.repository.IUsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service("customUserDetailsService")
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

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
                    return new UsernameNotFoundException("Usuario no encontrado: " + emailOrUser);
                });
        
        logger.info("Usuario encontrado: {}", usuario.getEmail());

        if (!usuario.isActivo()) {
            logger.warn("Intento de login de usuario inactivo: {}", emailOrUser);
            throw new UsernameNotFoundException("Usuario inactivo: " + emailOrUser);
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        if (usuario.getPermiso() != null && usuario.getPermiso().getNombre() != null) {
            authorities.add(new SimpleGrantedAuthority(usuario.getPermiso().getNombre()));
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_CLIENTE"));
        }
        
        logger.info("Roles asignados a {}: {}", emailOrUser, authorities);

        // Se pasa el objeto Usuario completo al constructor de CustomUserDetails
        return new CustomUserDetails(usuario, authorities);
    }
}