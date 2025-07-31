package com.analistas.luzclaritaweb.model.service.impl;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.analistas.luzclaritaweb.model.domain.Venta;
import com.analistas.luzclaritaweb.model.repository.IVentaRepository;
import com.analistas.luzclaritaweb.model.service.interfaces.IVentaService;

@Service
public class IVentaServiceImpl implements IVentaService {
    private final IVentaRepository ventaRepository;

    public IVentaServiceImpl(IVentaRepository ventaRepository) {
        this.ventaRepository = ventaRepository;
    }

    @Override
    public List<Venta> listarVentas() {
        return ventaRepository.findAll();
    }

    @Override
    public Optional<Venta> obtenerVentaPorId(Long id) {
        return ventaRepository.findById(id);
    }

    @Override
    public Venta guardarVenta(Venta venta) {
        return ventaRepository.save(venta);
    }

    @Override
    public void eliminarVenta(Long id) {
        ventaRepository.deleteById(id);
    }

    // Métodos personalizados
    @Override
    public List<Venta> buscarPorCliente(Long clienteId) {
        return ventaRepository.findByClienteId(clienteId);
    }

    @Override
    public List<Venta> buscarPorVendedor(Long vendedorId) {
        return ventaRepository.findByVendedorId(vendedorId);
    }

    @Override
    public List<Venta> buscarPorRangoDeFechas(LocalDateTime inicio, LocalDateTime fin) {
        return ventaRepository.findByFechaVentaBetween(inicio, fin);
    }

    @Override
    public List<Venta> buscarPorEstado(Venta.EstadoVenta estado) {
        return ventaRepository.findByEstado(estado);
    }

    @Override
    public List<Venta> buscarPorMetodoPago(String metodoPago) {
        return ventaRepository.findByMetodoPago(metodoPago);
    }
}
