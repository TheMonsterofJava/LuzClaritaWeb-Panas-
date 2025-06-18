package com.analistas.luzclaritaweb.model.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.analistas.luzclaritaweb.model.domain.Venta;

// Importante: Este servicio es una interfaz que define los métodos para manejar las operaciones de venta en la aplicación.
// La implementación de esta interfaz se encargará de la lógica de negocio y de interactuar con el repositorio correspondiente.
public interface IVentaService {
    List<Venta> listarVentas();
    Optional<Venta> obtenerVentaPorId(Long id);
    Venta guardarVenta(Venta venta);
    void eliminarVenta(Long id);

    // Métodos personalizados
    List<Venta> buscarPorCliente(Long clienteId);
    List<Venta> buscarPorVendedor(Long vendedorId);
    List<Venta> buscarPorRangoDeFechas(LocalDateTime inicio, LocalDateTime fin);
    List<Venta> buscarPorEstado(Venta.EstadoVenta estado);
    List<Venta> buscarPorMetodoPago(String metodoPago);
}
