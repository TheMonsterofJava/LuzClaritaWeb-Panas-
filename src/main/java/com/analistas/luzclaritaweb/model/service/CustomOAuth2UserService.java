package com.analistas.luzclaritaweb.model.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.analistas.luzclaritaweb.model.domain.Permiso;
import com.analistas.luzclaritaweb.model.domain.Usuario;
import com.analistas.luzclaritaweb.model.repository.IPermisoRepository;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private IUsuariosService usuariosService;

    @Autowired
    private IPermisoRepository permisoRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        // Extraer información del usuario de Facebook
        String email = oauth2User.getAttribute("email");
        String nombre = oauth2User.getAttribute("name");

        // Verificar si el usuario ya existe en la base de datos
        @SuppressWarnings("unused")
        Usuario usuario = usuariosService.findByEmail(email).orElseGet(() -> {
            // Crear un nuevo usuario si no existe
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setEmail(email);
            nuevoUsuario.setNomb_usu(nombre);
            nuevoUsuario.setClave(null); // No necesitamos contraseña para OAuth2
            nuevoUsuario.setFecha_creacion(new java.util.Date());
            nuevoUsuario.setActivo(true);

            // Asignar el permiso de "Cliente"
            Permiso permisoCliente = permisoRepository.findByNombre("ROLE_CLIENTE")
                    .orElseThrow(() -> new RuntimeException("Permiso 'ROLE_CLIENTE' no encontrado"));
            nuevoUsuario.setPermiso(permisoCliente);

            return usuariosService.guardarUsuario(nuevoUsuario);
        });

        return oauth2User;
    }
}