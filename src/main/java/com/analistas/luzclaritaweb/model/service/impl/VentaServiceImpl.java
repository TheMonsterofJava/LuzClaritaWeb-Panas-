package com.analistas.luzclaritaweb.model.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.analistas.luzclaritaweb.model.domain.DetalleVenta;
import com.analistas.luzclaritaweb.model.domain.Detalle_factura;
import com.analistas.luzclaritaweb.model.domain.Factura;
import com.analistas.luzclaritaweb.model.domain.Venta;
//import com.analistas.luzclaritaweb.model.repository.IDetalleVentaRepository;
import com.analistas.luzclaritaweb.model.repository.IVentaRepository;
import com.analistas.luzclaritaweb.model.service.interfaces.IVentaService;

@Service
public class VentaServiceImpl implements IVentaService {

    @Autowired
    private IVentaRepository ventaRepository;

    // @Autowired
    // private IDetalleVentaRepository detalleVentaRepository;

    @Override
    @Transactional
    public void crearVentaDesdeFactura(Factura factura) {
        Venta venta = new Venta();
        venta.setCliente(factura.getCliente());
        venta.setFechaVenta(factura.getFecha_pedido());
        venta.setMetodoPago(factura.getMetodo_pago());
        
        // Asumiendo que el usuario que paga es el vendedor en este contexto.
        // Si hay una lógica de negocio diferente, esto podría necesitar un ajuste.
        if (factura.getCliente() != null) {
            venta.setVendedor(factura.getCliente().getUsuario());
        }

        List<DetalleVenta> detallesVenta = new ArrayList<>();
        BigDecimal totalVenta = BigDecimal.ZERO;

        for (Detalle_factura detalleFactura : factura.getDetalles()) {
            DetalleVenta detalleVenta = new DetalleVenta();
            // Corregido para usar el ID del item y el tipo de item
            detalleVenta.setItemId(detalleFactura.getProducto().getId());
            detalleVenta.setTipoItem(DetalleVenta.TipoItem.PRODUCTO);
            detalleVenta.setCantidad(detalleFactura.getCantidad());
            detalleVenta.setPrecioUnitario(detalleFactura.getPrecio_unitario());
            detalleVenta.setVenta(venta);
            detallesVenta.add(detalleVenta);

            totalVenta = totalVenta.add(detalleFactura.getPrecio_unitario().multiply(new BigDecimal(detalleFactura.getCantidad())));
        }

        venta.setTotal(totalVenta);
        venta.setDetalles(detallesVenta);
        
        // El estado se establece a COMPLETADA por el @PrePersist en la entidad Venta
        
        ventaRepository.save(venta);
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

    @Override
    public List<Venta> buscarPorCliente(Long clienteId) {
        // La implementación requeriría un método en el repositorio, por ejemplo: findByClienteId(clienteId)
        return new ArrayList<>();
    }

    @Override
    public List<Venta> buscarPorVendedor(Long vendedorId) {
        // La implementación requeriría un método en el repositorio, por ejemplo: findByVendedorId(vendedorId)
        return new ArrayList<>();
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
