package com.analistas.luzclaritaweb.model.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.analistas.luzclaritaweb.model.domain.Carrito;
import com.analistas.luzclaritaweb.model.domain.Detalle_factura;
import com.analistas.luzclaritaweb.model.domain.Factura;
import com.analistas.luzclaritaweb.model.domain.Producto;
import com.analistas.luzclaritaweb.model.domain.Usuario;
import com.analistas.luzclaritaweb.model.repository.IDetalleFacturaRepository;
import com.analistas.luzclaritaweb.model.repository.IFacturaRepository;
import com.analistas.luzclaritaweb.model.repository.IProductoRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class IFacturaServiceImp implements IFacturaService {

    @Autowired
    private IFacturaRepository facturaRepository;

    @Autowired
    private IProductoRepository productoRepository;

    @Autowired
    private IDetalleFacturaRepository detalleFacturaRepository;

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

        Factura factura = new Factura();
        factura.setNumero_factura("FAC-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" +
                generarNumeroFactura());
        factura.setFecha_pedido(LocalDateTime.now());
        factura.setMetodo_pago(metodoPago);
        factura.setCliente(usuario.getCliente());
        factura.setActivo(true);

        List<Detalle_factura> detalles = new ArrayList<>();

        for (Carrito item : itemsCarrito) {
            Detalle_factura detalle = new Detalle_factura();
            detalle.setProducto(item.getProducto());
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecio_unitario(item.getProducto().getPrecio());
            detalle.setFactura(factura);
            detalles.add(detalle);
        }

        factura.setDetalles(detalles);
        return guardar(factura);
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
