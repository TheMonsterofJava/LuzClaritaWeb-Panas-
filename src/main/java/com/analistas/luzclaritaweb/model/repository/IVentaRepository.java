package com.analistas.luzclaritaweb.model.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.analistas.luzclaritaweb.model.domain.Venta;

@Repository
public interface IVentaRepository extends JpaRepository<Venta, Long> {

    // Buscar ventas de un cliente específico
    List<Venta> findByClienteId(Long clienteId);

    // Buscar ventas de un vendedor en particular
    List<Venta> findByVendedorId(Long vendedorId);

    // Buscar ventas en un rango de fechas
    List<Venta> findByFechaVentaBetween(LocalDateTime inicio, LocalDateTime fin);

    // Buscar ventas por estado (PENDIENTE, COMPLETADA, CANCELADA)
    List<Venta> findByEstado(Venta.EstadoVenta estado);

    // Buscar ventas por método de pago (Efectivo, Tarjeta, etc.)
    List<Venta> findByMetodoPago(String metodoPago);

}
