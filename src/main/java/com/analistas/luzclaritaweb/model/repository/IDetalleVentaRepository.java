package com.analistas.luzclaritaweb.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.analistas.luzclaritaweb.model.domain.DetalleVenta;

@Repository
public interface IDetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {

    // Buscar todos los detalles de una venta en particular
    List<DetalleVenta> findByVentaId(Long ventaId);

    // Buscar detalles por tipo de ítem (PRODUCTO o CURSO)
    List<DetalleVenta> findByTipoItem(DetalleVenta.TipoItem tipoItem);

    // Buscar detalles de venta por ID de ítem
    List<DetalleVenta> findByItemId(Long itemId);

}
