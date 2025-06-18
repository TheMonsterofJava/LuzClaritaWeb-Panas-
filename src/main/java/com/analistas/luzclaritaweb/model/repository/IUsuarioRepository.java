package com.analistas.luzclaritaweb.model.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.analistas.luzclaritaweb.model.domain.Usuario;

@Repository
public interface IUsuarioRepository extends JpaRepository<Usuario, Long> {

    // Verificar si el usuario existe por su email
    Optional<Usuario> findByEmail(String email);

    // Buscar usuario por nombre de usuario (nomb_usu) usando una consulta
    // personalizada
    @Query("SELECT u FROM Usuario u WHERE u.nomb_usu = :nombUsu")
    Optional<Usuario> findByNombUsu(@Param("nombUsu") String nombUsu);

    @Query("SELECT u FROM Usuario u WHERE u.email = :email")
    Optional<Usuario> findByEmail2(@Param("email") String email);

    @Query("SELECT u FROM Usuario u WHERE (u.email = :emailOrUser OR u.nomb_usu = :emailOrUser) AND u.clave = :password")
    Usuario findByEmailOrUSerAndPassword(@Param("emailOrUser") String emailOrUser, @Param("password") String password);

    // Consulta personalizada para eliminar un usuario por ID
    @Modifying
    @Query("DELETE FROM Usuario u WHERE u.id = :id")
    void eliminarUsuario(@Param("id") Long id);

    // Conuslta a la base de datos para saber si existe un usuario en la base de
    // datos
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
            "FROM Usuario u WHERE u.email = ?1 OR u.nomb_usu = ?1")
    boolean existsByEmailOrNombUsu(String identificador);

}

// fin de la interface IUsuarioRepository

// @Query("SELECT u FROM Usuario u WHERE u.email = :email OR u.nomb_usu =
// :nomb_usu AND u.clave = :clave")
// Usuario findByUsernameEmailandPassword(@Param("email") String email,
// @Param("nomb_usu") String nomb_usu, @Param("clave") String clave);

// Buscar usuario por nombre y contraseña
// @Query("SELECT u FROM Usuario u WHERE u.nomb_usu = :nomb_usu AND u.clave =
// :clave")
// Usuario findByUsernameAndPassword(@Param("nomb_usu") String nomb_usu,
// @Param("clave") String clave);

// Buscar por Nombre de Usuario y por Email en la BD
// @Query("SELECT u FROM Usuario u WHERE u.nomb_usu = :UserOrEmail OR u.email =
// :UserOrEmail")
// Usuario findByUsernameAndEmail(@Param("UserOrEmail") String nomb_usu,
// @Param("UserOrEmail") String email);

// Buscar usuario por email y contraseña
// @Query("SELECT u FROM Usuario u WHERE u.email = :email AND u.clave = :clave")
// Usuario findByEmailAndPassword(@Param("email") String email, @Param("clave")
// String clave);