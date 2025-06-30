package com.analistas.luzclaritaweb.model.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.analistas.luzclaritaweb.model.domain.Cliente;
import com.analistas.luzclaritaweb.model.domain.Permiso;
import com.analistas.luzclaritaweb.model.domain.Usuario;
import com.analistas.luzclaritaweb.model.repository.IClienteRepository;
import com.analistas.luzclaritaweb.model.repository.IPermisoRepository;
import com.analistas.luzclaritaweb.model.repository.IUsuarioRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IUsuariosService {

    private final IUsuarioRepository usuarioRepository;
    private final IPermisoRepository permisoRepository;
    private final IClienteRepository clienteRepository;

    public Optional<Usuario> findById(Long id) {
        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public Optional<Usuario> findByEmail2(String email) {
        return usuarioRepository.findByEmail(email);
    }

    // Método para buscar usuario por nombre de usuario (nomb_usu)
    public Optional<Usuario> findByNombUsu(String nomb_usu) {
        return usuarioRepository.findByNombUsu(nomb_usu);
    }

    public Usuario guardarUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    public List<Permiso> findAllPermisos() {
        return permisoRepository.findAll();
    }

    public List<Usuario> findAll() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        System.out.println("Usuarios en el servicio: " + usuarios);
        return usuarios;
    }

    public Permiso obtenerPermisoPorNombre(String nombrePermiso) {
        return permisoRepository.findByNombre(nombrePermiso)
                .orElseThrow(() -> new RuntimeException("Permiso no encontrado: " + nombrePermiso));
    }

    public Object findPermisoById(Long permisoId) {
        return permisoRepository.findById(permisoId)
                .orElseThrow(() -> new RuntimeException("Permiso no encontrado con ID: " + permisoId));
    }

    public Usuario findByEmailOrUSerAndPassword(String emailOrUser, String password) {
        return usuarioRepository.findByEmailOrUSerAndPassword(emailOrUser, password);
    }

    // buscar por nombre de usuario
    public Optional<Usuario> buscarPorNombreUsuario(String nomb_usu) {
        return usuarioRepository.findByNombUsu(nomb_usu);
    }

    @Transactional
    public void eliminarUsuario(Long id) {
        // Buscar el usuario por ID
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Verificar si existe un cliente asociado
        Optional<Cliente> clienteOpt = clienteRepository.findByUsuario(usuario);
        if (clienteOpt.isPresent()) {
            clienteRepository.delete(clienteOpt.get());
        }

        // Eliminar el usuario
        usuarioRepository.delete(usuario);

    }

    // Metodo para ver si existe un usuario en la base de datos
    public boolean existeUsuario(String emailOrUser) {
        return usuarioRepository.existsByEmailOrNombUsu(emailOrUser);
    }
}

// @Service
// @RequiredArgsConstructor
// public class IUsuariosService {

// private final IUsuarioRepository usuarioRepository;
// private final IPermisoRepository permisoRepository; // Inyectar el
// repositorio de permisos

// public Optional<Usuario> findById(Long id) {
// return usuarioRepository.findById(id);
// }

// public Optional<Usuario> findByEmail(String email) {
// return usuarioRepository.findByEmail(email);
// }

// public Usuario guardarUsuario(Usuario usuario) {
// return usuarioRepository.save(usuario);
// }

// public List<Permiso> findAllPermisos() {
// return permisoRepository.findAll();
// }

// public List<Usuario> findAll() {
// List<Usuario> usuarios = usuarioRepository.findAll();
// System.out.println("Usuarios en el servicio: " + usuarios);
// return usuarios;
// }

// // Nuevo método para obtener un permiso por nombre
// public Permiso obtenerPermisoPorNombre(String nombrePermiso) {
// return permisoRepository.findByNombre(nombrePermiso)
// .orElseThrow(() -> new RuntimeException("Permiso no encontrado: " +
// nombrePermiso));
// }

// public Object findPermisoById(Long permisoId) {

// return permisoRepository.findById(permisoId)
// .orElseThrow(() -> new RuntimeException("Permiso no encontrado con ID: " +
// permisoId));

// }

// //Buscar por email o usuario...
// public Usuario findByEmailOrUSerAndPassword(String emailOrUser, String
// password) {
// return usuarioRepository.findByEmailOrUSerAndPassword(emailOrUser, password);
// }

// public void eliminarUsuario(Long id) {
// usuarioRepository.eliminarUsuario(id);
// }

// }