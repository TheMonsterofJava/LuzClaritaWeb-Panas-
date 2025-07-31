package com.analistas.luzclaritaweb.web.config.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

//Clase que sirve para crear un objeto de usuario personalizado
// Extiende User de Spring Security para incluir información adicional del usuario
public class CustomUserDetails extends org.springframework.security.core.userdetails.User {

    private final Long userId;
    private final String realusername;

    public CustomUserDetails(
        String username,
        String password,
        boolean enabled,
        boolean accountNonExpired,
        boolean credentialsNonExpired,
        boolean accountNonLocked,
        Collection<? extends GrantedAuthority> authorities,
        Long userId,
        String realusername) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked,
            authorities);
        this.userId = userId;
        this.realusername = realusername;
    }

    public Long getUserId() {
        return userId;
    }

    public String getRealUsername() {
        return realusername;
    }

}
