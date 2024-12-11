package com.analistas.luzclaritaweb.model.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.analistas.luzclaritaweb.model.domain.Usuario;

@Repository
public interface IUsuarioRepository extends JpaRepository<Usuario, Long> {

    // Verificar si el usuario existe por su email

    Optional<Usuario> findByEmail(String email);

}
