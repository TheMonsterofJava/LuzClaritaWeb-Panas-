package com.analistas.luzclaritaweb.model.service.interfaces;

import java.util.List;
import java.util.Optional;

import com.analistas.luzclaritaweb.model.domain.DetalleCompra;

public interface IDetalleCompraService {

    List<DetalleCompra> listarDetallesCompra();

    Optional<DetalleCompra> obtenerDetallePorId(Long id);

    List<DetalleCompra> obtenerDetallesPorCompra(Long compraId);

    DetalleCompra guardarDetalleCompra(DetalleCompra detalleCompra);

    void eliminarDetalleCompra(Long id);

}
