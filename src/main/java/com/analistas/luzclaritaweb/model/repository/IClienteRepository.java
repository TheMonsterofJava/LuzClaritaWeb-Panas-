package com.analistas.luzclaritaweb.model.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.analistas.luzclaritaweb.model.domain.Cliente;
import com.analistas.luzclaritaweb.model.domain.Usuario;

public interface IClienteRepository extends JpaRepository <Cliente, Long>{
    // Métodos adicionales por si se quiere buscar clientes por nombre, correo, etc.
    
        Optional<Cliente> findByCorreo(String correo);

        //Buscar usuario
        Optional<Cliente> findByUsuario(Usuario usuario);
}
