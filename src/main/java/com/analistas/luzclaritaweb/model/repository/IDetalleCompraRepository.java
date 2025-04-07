package com.analistas.luzclaritaweb.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.analistas.luzclaritaweb.model.domain.DetalleCompra;

//Estaba en entero lo hiciste long 
public interface IDetalleCompraRepository extends JpaRepository<DetalleCompra, Long> {

    // Buscar detalles de compra por ID de compra
    List<DetalleCompra> findByCompraId(Long compraId);

}
