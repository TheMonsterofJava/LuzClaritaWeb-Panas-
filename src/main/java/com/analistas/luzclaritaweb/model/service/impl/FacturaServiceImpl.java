package com.analistas.luzclaritaweb.model.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.analistas.luzclaritaweb.model.domain.Caja;
import com.analistas.luzclaritaweb.model.domain.Carrito;
import com.analistas.luzclaritaweb.model.domain.Detalle_factura;
import com.analistas.luzclaritaweb.model.domain.Factura;
import com.analistas.luzclaritaweb.model.domain.Producto;
import com.analistas.luzclaritaweb.model.domain.Usuario;
import com.analistas.luzclaritaweb.model.repository.IDetalleFacturaRepository;
import com.analistas.luzclaritaweb.model.repository.IFacturaRepository;
import com.analistas.luzclaritaweb.model.repository.IProductoRepository;
import com.analistas.luzclaritaweb.model.service.interfaces.ICajaService;
import com.analistas.luzclaritaweb.model.service.interfaces.IFacturaService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class FacturaServiceImpl implements IFacturaService {

    @Autowired
    private IFacturaRepository facturaRepository;

    @Autowired
    private IProductoRepository productoRepository;

    @Autowired
    private IDetalleFacturaRepository detalleFacturaRepository;

    @Autowired
    private ICajaService cajaService;

    @Override
    public Factura guardar(Factura factura) {
        // Guardar la factura primero
        Factura facturaGuardada = facturaRepository.save(factura);

        // Guardar los detalles
        for (Detalle_factura detalle : factura.getDetalles()) {
            detalle.setFactura(facturaGuardada);
            detalleFacturaRepository.save(detalle);
        }

        return facturaGuardada;
    }

    @Override
    public Factura buscarPorId(Long id) {
        return facturaRepository.findById(id).orElse(null);
    }

    @Override
    public List<Factura> buscarTodas() {
        return facturaRepository.findAll();
    }

    @Override
    public List<Factura> buscarPorUsuario(Usuario usuario) {
        return facturaRepository.findByClienteUsuario(usuario);
    }

    @Override
    public Factura crearFacturaDesdeCarrito(List<Carrito> itemsCarrito, Usuario usuario, String metodoPago) {
        if (usuario.getCliente() == null) {
            throw new IllegalStateException("El usuario no tiene un cliente asociado");
        }

        //Obtener una caja activa para registrar la factura
         // Obtener la caja activa
        Caja cajaActiva = cajaService.buscarUltimaCajaAbiertaYActiva(Caja.EstadoCaja.ABIERTA)
                .orElseThrow(() -> new IllegalStateException("No hay ninguna caja activa en el sistema. No se puede crear la factura."));

        Factura factura = new Factura();
        factura.setNumero_factura("FAC-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" +
                generarNumeroFactura());
        factura.setFecha_pedido(LocalDateTime.now());
        factura.setMetodo_pago(metodoPago);
        factura.setCliente(usuario.getCliente());
        factura.setActivo(true);
        factura.setCaja(cajaActiva); //Asignamos la Caja Activa a la factura.

        //Esta linea de codigo es para que la factura se guarde con el estado de la caja activa.
        List<Detalle_factura> detalles = new ArrayList<>();
        BigDecimal totalFactura = BigDecimal.ZERO;

        for (Carrito item : itemsCarrito) {
            Detalle_factura detalle = new Detalle_factura();
            Producto productoDelCarrito = item.getProducto(); // Obtenemos el producto del carrito
            //Estamos trayendo el producto del carrito 
            //Pero tambien seria bueno traerlo desde la base de datos para asegurarnos de que el stock es correcto
            // productoRepository.findById(productoDelCarrito.getId()) y usar su precio.

            detalle.setProducto(item.getProducto());
            detalle.setCantidad(item.getCantidad());
            //detalle.setPrecio_unitario(item.getProducto().getPrecio());
            detalle.setPrecio_unitario(productoDelCarrito.getPrecio()); // Usamos el precio del producto del carrito
            detalle.setFactura(factura);
            detalles.add(detalle);

            totalFactura = totalFactura.add(productoDelCarrito.getPrecio().multiply(BigDecimal.valueOf(item.getCantidad())));
        }

        // Aunque Factura tiene un método calcularTotal(), MercadoPago ya validó el monto.
        // El total de la factura se podría setear aquí también si fuera necesario persistirlo directamente en Factura.
        // factura.setTotal(totalFactura); // Si la entidad Factura tuviera un campo 'total'
        factura.setDetalles(detalles);
        return guardar(factura); // El método guardar ya es transaccional
    }

    @Override
    public String generarNumeroFactura() {
        Long ultimoNumero = facturaRepository.countByFechaPedidoBetween(
                LocalDateTime.now().withHour(0).withMinute(0).withSecond(0),
                LocalDateTime.now());
        // Formateamos el número con 4 dígitos, rellenando con ceros a la izquierda
        return String.format("%04d", ultimoNumero + 1);
    }

    @Override
    @Transactional
    public void actualizarInventario(List<Carrito> itemsCarrito) {
        for (Carrito item : itemsCarrito) {
            Producto producto = productoRepository.findById(item.getProducto().getId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            if (producto.getStock() < item.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para " + producto.getDescripcion());
            }

            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepository.save(producto);
        }
    }
}
