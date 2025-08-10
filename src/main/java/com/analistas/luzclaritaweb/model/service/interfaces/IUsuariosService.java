package com.analistas.luzclaritaweb.model.service.interfaces;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.analistas.luzclaritaweb.model.domain.Cliente;
import com.analistas.luzclaritaweb.model.domain.Permiso;
import com.analistas.luzclaritaweb.model.domain.Usuario;
import com.analistas.luzclaritaweb.model.repository.IClienteRepository;
import com.analistas.luzclaritaweb.model.repository.IPermisoRepository;
import com.analistas.luzclaritaweb.model.repository.IUsuarioRepository;

//Agregamos imprts para la paginacion en el Dashboard
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IUsuariosService {

    private final IUsuarioRepository usuarioRepository;
    private final IPermisoRepository permisoRepository;
    private final IClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    // Actualizar informacion personal en el perfil
    public void updatePersonalInfo(Cliente cliente) {
        clienteRepository.save(cliente);
    }

    // Cambiar contraseña en el perfil
    public void changePassword(Usuario usuario, String currentPassword, String newPassword) {
        // Validar que la nueva contraseña no sea igual a la actual
        if (passwordEncoder.matches(newPassword, usuario.getClave())) {
            throw new IllegalStateException("La nueva contraseña no puede ser igual a la actual");
        }

        if (!passwordEncoder.matches(currentPassword, usuario.getClave())) {
            throw new IllegalStateException("La contraseña actual es incorrecta.");
        }

        usuario.setClave(passwordEncoder.encode(newPassword));
        usuarioRepository.save(usuario);
    }

    // actualizar el nombre y el email
    public void updateNameAndEmail(Usuario usuario, String nomb_usu, String email) {
        usuario.setNomb_usu(nomb_usu);
        usuario.setEmail(email);
        usuarioRepository.save(usuario);
    }


    //Paginacion en el Dashboard
     public Page<Usuario> findAll(Pageable pageable) {
        return usuarioRepository.findAll(pageable);
    }

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

    // public Usuario findByEmailOrUSerAndPassword(String emailOrUser, String
    // password) {
    // return usuarioRepository.findByEmailOrUSerAndPassword(emailOrUser, password);
    // }

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

    // // NUEVO MÉTODO - Buscar por nombre de usuario O email para el
    // CustomUserDetailsService
    // public Usuario buscarPorNombreUsuarioOEmail(String username) {
    // // Primero buscar por email
    // Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(username);

    // // Si no se encuentra por email, buscar por nombre de usuario
    // if (!usuarioOpt.isPresent()) {
    // usuarioOpt = usuarioRepository.findByNombUsu(username);
    // }

    // return usuarioOpt.orElse(null);
    // }

    // NUEVO MÉTODO - Buscar por nombre de usuario O email
    @Transactional(readOnly = true)
    public Usuario buscarPorNombreUsuarioOEmail(String username) {
        // Primero buscar por email
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(username);

        // Si no se encuentra por email, buscar por nombre de usuario
        if (!usuarioOpt.isPresent()) {
            usuarioOpt = usuarioRepository.findByNombUsu(username);
        }

        return usuarioOpt.orElse(null);
    }

    public Optional<Usuario> existsByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    @Transactional
    public void updateAccountInfo(Usuario usuario, String newUsername, String newEmail) {
        // Verificar si el nuevo email ya está en uso por otro usuario
        Optional<Usuario> userByEmail = usuarioRepository.findByEmail(newEmail);
        if (userByEmail.isPresent() && !userByEmail.get().getId().equals(usuario.getId())) {
            throw new IllegalStateException("El email ya está en uso por otra cuenta.");
        }

        // Verificar si el nuevo nombre de usuario ya está en uso por otro usuario
        Optional<Usuario> userByUsername = usuarioRepository.findByNombUsu(newUsername);
        if (userByUsername.isPresent() && !userByUsername.get().getId().equals(usuario.getId())) {
            throw new IllegalStateException("El nombre de usuario ya está en uso por otra cuenta.");
        }

        usuario.setNomb_usu(newUsername);
        usuario.setEmail(newEmail);
        usuarioRepository.save(usuario);
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