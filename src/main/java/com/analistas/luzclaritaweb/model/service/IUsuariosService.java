package com.analistas.luzclaritaweb.model.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.analistas.luzclaritaweb.model.domain.Permiso;
import com.analistas.luzclaritaweb.model.domain.Usuario;
import com.analistas.luzclaritaweb.model.repository.IPermisoRepository;
import com.analistas.luzclaritaweb.model.repository.IUsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IUsuariosService {

    private final IUsuarioRepository usuarioRepository;
    private final IPermisoRepository permisoRepository; // Inyectar el repositorio de permisos

    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public Usuario guardarUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    // Nuevo método para obtener un permiso por nombre
    public Permiso obtenerPermisoPorNombre(String nombrePermiso) {
        return permisoRepository.findByNombre(nombrePermiso)
                .orElseThrow(() -> new RuntimeException("Permiso no encontrado: " + nombrePermiso));
    }
}
