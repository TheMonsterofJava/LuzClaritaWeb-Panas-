package com.analistas.luzclaritaweb.model.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.analistas.luzclaritaweb.model.domain.Caja;
import com.analistas.luzclaritaweb.model.domain.DetalleVenta;
import com.analistas.luzclaritaweb.model.domain.Detalle_factura;
import com.analistas.luzclaritaweb.model.domain.Factura;
import com.analistas.luzclaritaweb.model.domain.MovimientoCaja;
import com.analistas.luzclaritaweb.model.domain.Producto;
import com.analistas.luzclaritaweb.model.domain.Receta;
import com.analistas.luzclaritaweb.model.domain.Venta;
import com.analistas.luzclaritaweb.model.repository.IFacturaRepository;
import com.analistas.luzclaritaweb.model.repository.IProductoRepository;
import com.analistas.luzclaritaweb.model.repository.IRecetaRepository;
import com.analistas.luzclaritaweb.model.repository.IVentaRepository;
import com.analistas.luzclaritaweb.model.service.interfaces.ICajaService;
import com.analistas.luzclaritaweb.model.service.interfaces.IMovimientoCajaService;
import com.analistas.luzclaritaweb.model.service.interfaces.IVentaService;
import com.analistas.luzclaritaweb.web.excepciones.StockInsuficienteException;

@Service
public class VentaServiceImpl implements IVentaService {

    @Autowired
    private IVentaRepository ventaRepository;

    @Autowired
    private IProductoRepository productoRepository;

    @Autowired
    private IFacturaRepository facturaRepository;

    @Autowired
    private IMovimientoCajaService movimientoCajaService;

    @Autowired
    private ICajaService cajaService;

    @Autowired
    private IRecetaRepository 
    recetaRepository ;

    // @Autowired
    // private IDetalleVentaRepository detalleVentaRepository;

    @Override
    @Transactional
    public void crearVentaDesdeFactura(Factura factura) {
        Venta venta = new Venta();
        venta.setCliente(factura.getCliente());
        venta.setFechaVenta(factura.getFechapedido());
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

            totalVenta = totalVenta
                    .add(detalleFactura.getPrecio_unitario().multiply(new BigDecimal(detalleFactura.getCantidad())));
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
        return ventaRepository.findByClienteId(clienteId);
    }

    @Override
    public List<Venta> buscarPorVendedor(Long vendedorId) {
        // La implementación requeriría un método en el repositorio, por ejemplo:
        // findByVendedorId(vendedorId)
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

    @Override
    public Venta buscarPorId(Long id) {
        return ventaRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void procesarVentaExitosa(Long ventaId) {
        Venta venta = buscarPorId(ventaId);
        if (venta == null || venta.getEstado() != Venta.EstadoVenta.PENDIENTE) {
            // Si la venta no existe o ya fue procesada, no hacer nada.
            return;
        }

        // 1. Cambiar estado de la venta
        venta.setEstado(Venta.EstadoVenta.COMPLETADA);
        venta.setFechaVenta(LocalDateTime.now());

        // 2. Descontar stock solo para productos
        for (DetalleVenta detalle : venta.getDetalles()) {
            if (detalle.getTipoItem() == DetalleVenta.TipoItem.PRODUCTO) {
                Producto producto = productoRepository.findById(detalle.getItemId())
                        .orElseThrow(
                                () -> new RuntimeException("Producto no encontrado con ID: " + detalle.getItemId()));

                if (producto.getStock() < detalle.getCantidad()) {
                    throw new StockInsuficienteException(
                            "Stock insuficiente para el producto: " + producto.getDescripcion());
                }
                producto.setStock(producto.getStock() - detalle.getCantidad());
                productoRepository.save(producto);
            }
        }

        // 3. Crear Factura
        Factura factura = new Factura();
        factura.setNumero_factura("F-" + venta.getId()); // Asignar número de factura único
        factura.setCliente(venta.getCliente());
        factura.setFechapedido(LocalDateTime.now());
        factura.setMetodo_pago(venta.getMetodoPago());
        factura.setActivo(true);
        // Asociar la caja de ventas activa
        factura.setCaja(cajaService.obtenerCajaActivaParaVentas());
        // Aquí podrías añadir el collection_id y external_reference si los guardas en
        // la Venta

        List<Detalle_factura> detallesFactura = new ArrayList<>();
        for (DetalleVenta detalleVenta : venta.getDetalles()) {
            Detalle_factura detalleFactura = new Detalle_factura();
            detalleFactura.setFactura(factura);
            detalleFactura.setCantidad(detalleVenta.getCantidad());
            detalleFactura.setPrecio_unitario(detalleVenta.getPrecioUnitario());

            if (detalleVenta.getTipoItem() == DetalleVenta.TipoItem.PRODUCTO) {
                Producto producto = productoRepository.findById(detalleVenta.getItemId()).get();
                detalleFactura.setProducto(producto);
            } else { // Es una receta
                Receta receta = recetaRepository.findById(detalleVenta.getItemId()).get();
                detalleFactura.setReceta(receta);
            }

            detallesFactura.add(detalleFactura);
        }
        factura.setDetalles(detallesFactura);
        facturaRepository.save(factura);

        // 4. Crear Movimiento de Caja
        MovimientoCaja movimiento = new MovimientoCaja();
        movimiento.setCaja(factura.getCaja());
        movimiento.setTipoOperacion(MovimientoCaja.TipoOperacion.INGRESO);
        movimiento.setMonto(venta.getTotal());
        movimiento.setMontoDouble(venta.getTotal().doubleValue());
        movimiento.setDescripcion("Ingreso por Venta #" + venta.getId());
        movimiento.setOperador(venta.getVendedor());
        movimiento.setVenta(venta);
        movimiento.setFactura(factura);
        movimiento.setTipo("INGRESO");
        movimientoCajaService.guardarMovimiento(movimiento);

        // 5. Actualizar Saldo de Caja
        Caja caja = factura.getCaja();
        if (caja != null) {
            caja.setSaldoFinal(caja.getSaldoFinal().add(venta.getTotal()));
            cajaService.guardarCaja(caja);
        }

        // Guardar la venta actualizada
        ventaRepository.save(venta);
    }
}
