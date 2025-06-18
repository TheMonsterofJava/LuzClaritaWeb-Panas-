package com.analistas.luzclaritaweb.model.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.analistas.luzclaritaweb.model.domain.Compra;

@Repository
public interface ICompraRepository extends JpaRepository<Compra, Long> {

    // Buscar compras en un rango de fechas
    List<Compra> findByFechaHoraBetween(LocalDateTime inicio, LocalDateTime fin);

    // Buscar compras por usuario
    List<Compra> findByUsuarioId(Long usuarioId);

    // Buscar compras por proveedor
    List<Compra> findByProveedorId(Long proveedorId);

    // Buscar compras por caja
    List<Compra> findByCajaId(Long cajaId);

     // Buscar compras activas
     List<Compra> findByActivoTrue();
}
