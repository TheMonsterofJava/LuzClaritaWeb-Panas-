package com.analistas.luzclaritaweb.web.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import com.analistas.luzclaritaweb.model.domain.Permiso;
import com.analistas.luzclaritaweb.model.domain.Usuario;
import com.analistas.luzclaritaweb.model.repository.IPermisoRepository;
import com.analistas.luzclaritaweb.model.service.interfaces.IUsuariosService;

@Service
public class CustomOidcUserService extends OidcUserService {

    @Autowired
    private IUsuariosService usuariosService;

    @Autowired
    private IPermisoRepository permisoRepository;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        // Extraer información del usuario de Google
        String email = oidcUser.getEmail();
        String nombre = oidcUser.getFullName();

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
            Permiso permisoCliente = permisoRepository.findByNombre("CLIENTE")
                    .orElseThrow(() -> new RuntimeException("Permiso 'CLIENTE' no encontrado"));
            nuevoUsuario.setPermiso(permisoCliente);

            return usuariosService.guardarUsuario(nuevoUsuario);
        });

        return oidcUser;
    }
}