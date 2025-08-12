package com.analistas.luzclaritaweb.model.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.analistas.luzclaritaweb.model.domain.Carrito;


@Repository
public interface ICarritoRepository extends JpaRepository<Carrito, Long> {
    List<Carrito> findByUsuarioId(Long usuarioId);
    Optional<Carrito> findByUsuarioIdAndProductoId(Long usuarioId, Long productoId);
    Optional<Carrito> findByUsuarioIdAndRecetaId(Long usuarioId, Long recetaId);
}