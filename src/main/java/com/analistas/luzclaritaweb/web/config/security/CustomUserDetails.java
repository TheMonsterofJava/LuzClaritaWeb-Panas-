package com.analistas.luzclaritaweb.web.config.security;

import com.analistas.luzclaritaweb.model.domain.Usuario;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class CustomUserDetails extends org.springframework.security.core.userdetails.User {

    private final Usuario usuario;

    public CustomUserDetails(
            Usuario usuario,
            Collection<? extends GrantedAuthority> authorities) {
        super(usuario.getEmail(), usuario.getClave(), usuario.isActivo(), true, true, true, authorities);
        this.usuario = usuario;
    }

    public Long getUserId() {
        return usuario.getId();
    }

    public String getRealUsername() {
        return usuario.getNomb_usu();
    }

    public Usuario getUsuario() {
        return this.usuario;
    }
}
