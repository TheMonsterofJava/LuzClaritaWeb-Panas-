package com.analistas.luzclaritaweb.web.security;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.analistas.luzclaritaweb.model.domain.Permiso;
import com.analistas.luzclaritaweb.model.domain.Usuario;
import com.analistas.luzclaritaweb.model.repository.IPermisoRepository;
import com.analistas.luzclaritaweb.model.repository.IUsuarioRepository;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private IUsuarioRepository usuarioRepository;

    // Necesitaremos un repositorio para permisos si queremos asignar uno por defecto
    // Asumiré que existe o lo buscaré. Si no, tendré que inyectar el EntityManager o usar un valor fijo.
    // Por ahora dejaré un comentario para verificar si existe IPermisoRepository.
    @Autowired
    private IPermisoRepository permisoRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // Buscar usuario por email
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);

        if (usuario == null) {
            usuario = new Usuario();
            usuario.setEmail(email);
            usuario.setNombre(name);
            usuario.setClave(""); // No tiene password local
            usuario.setActivo(true);
            usuario.setFecha_creacion(new java.util.Date());

            // Asignar permiso por defecto (Cliente)
            Permiso permisoCliente = permisoRepository.findByNombre("Cliente").orElse(null);

            // Si no existe, intentar buscar 'ROLE_USER' o el primer permiso disponible, o crear uno temporal
            // (Para este ejemplo asumo que 'Cliente' existe en la BD según lógica de negocio común)

            usuario.setPermiso(permisoCliente);

            usuarioRepository.save(usuario);
        }

        // Mapear autoridades
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        if (usuario.getPermiso() != null) {
            authorities.add(new SimpleGrantedAuthority(usuario.getPermiso().getNombre()));
        } else {
             authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return new DefaultOAuth2User(authorities, oAuth2User.getAttributes(), "email");
    }
}
