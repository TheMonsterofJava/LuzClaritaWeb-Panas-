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

    public Optional<Usuario> findById(Long id) {
        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
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

    // Nuevo método para obtener un permiso por nombre
    public Permiso obtenerPermisoPorNombre(String nombrePermiso) {
        return permisoRepository.findByNombre(nombrePermiso)
                .orElseThrow(() -> new RuntimeException("Permiso no encontrado: " + nombrePermiso));
    }

    public Object findPermisoById(Long permisoId) {

        return permisoRepository.findById(permisoId)
                .orElseThrow(() -> new RuntimeException("Permiso no encontrado con ID: " + permisoId));

    }

    //Buscar por email o usuario...
    public Usuario findByEmailOrUSerAndPassword(String emailOrUser, String password) {
        return usuarioRepository.findByEmailOrUSerAndPassword(emailOrUser, password);
    }
    
   

    // public Usuario findByUsernameAndPassword(String nomb_usu, String clave) {
    // return usuarioRepository.findByUsernameAndPassword(nomb_usu, clave);
    // }

    // public Usuario findByEmailAndPassword(String email, String clave) {
    // return usuarioRepository.findByUsernameAndPassword(email, clave);
    // }

    // public Usuario findByEmailAndPassword(String email, String clave) {
    // return usuarioRepository.findByEmailAndPassword(email, clave);
    // }

    // public Usuario findByUsernameAndEmail(String nomb_usu, String email){
    // return usuarioRepository.findByUsernameAndEmail(nomb_usu, email);
    // }

}
