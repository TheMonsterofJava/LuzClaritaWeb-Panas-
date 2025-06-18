package com.analistas.luzclaritaweb.model.service;

import java.util.List;
import java.util.Optional;

import com.analistas.luzclaritaweb.model.domain.DetalleVenta;

public interface IDetalleVentaService {
    List<DetalleVenta> listarDetalles();
    Optional<DetalleVenta> obtenerDetallePorId(Long id);
    DetalleVenta guardarDetalle(DetalleVenta detalle);
    void eliminarDetalle(Long id);

    // Métodos personalizados
    List<DetalleVenta> buscarPorVenta(Long ventaId);
    List<DetalleVenta> buscarPorTipoItem(DetalleVenta.TipoItem tipoItem);
    List<DetalleVenta> buscarPorItem(Long itemId);
}

