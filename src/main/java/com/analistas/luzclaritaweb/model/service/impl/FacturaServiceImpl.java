package com.analistas.luzclaritaweb.model.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.analistas.luzclaritaweb.dto.CarritoDTO;
import com.analistas.luzclaritaweb.model.domain.Caja;
import com.analistas.luzclaritaweb.model.domain.Detalle_factura;
import com.analistas.luzclaritaweb.model.domain.Factura;
import com.analistas.luzclaritaweb.model.domain.Producto;
import com.analistas.luzclaritaweb.model.domain.Usuario;
import com.analistas.luzclaritaweb.model.repository.IDetalleFacturaRepository;
import com.analistas.luzclaritaweb.model.repository.IFacturaRepository;
import com.analistas.luzclaritaweb.model.repository.IProductoRepository;
import com.analistas.luzclaritaweb.model.service.interfaces.ICajaService;
import com.analistas.luzclaritaweb.model.service.interfaces.IFacturaService;
import com.analistas.luzclaritaweb.web.excepciones.StockInsuficienteException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
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
    @Transactional
    public Factura guardar(Factura factura) {
        // Guardar la factura primero
        Factura facturaGuardada = facturaRepository.save(factura);

        // Guardar los detalles
        if (factura.getDetalles() != null) {
            for (Detalle_factura detalle : factura.getDetalles()) {
                detalle.setFactura(facturaGuardada);
                detalleFacturaRepository.save(detalle);
            }
        }

        return facturaGuardada;
    }

    @Override
    @Transactional(readOnly = true)
    public Factura buscarPorId(Long id) {
        return facturaRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Factura> buscarTodas() {
        return facturaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Factura> buscarPorUsuario(Usuario usuario) {
        return facturaRepository.findByClienteUsuario(usuario);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Factura crearFacturaDesdeCarrito(List<CarritoDTO> itemsCarrito, Usuario usuario, String metodoPago) {
        log.info("Iniciando creación de factura para usuario: {} con {} items.", usuario.getNomb_usu(), itemsCarrito.size());

        if (usuario.getCliente() == null) {
            log.error("Error crítico: El usuario {} no tiene un cliente asociado.", usuario.getNomb_usu());
            throw new IllegalStateException("El usuario no tiene un cliente asociado");
        }

        Caja cajaActiva = cajaService.buscarUltimaCajaAbiertaYActiva(Caja.EstadoCaja.ABIERTA)
                .orElseThrow(() -> {
                    log.error("Error crítico: No hay ninguna caja activa en el sistema.");
                    return new IllegalStateException("No hay ninguna caja activa en el sistema. No se puede crear la factura.");
                });
        log.info("Caja activa encontrada: ID {}", cajaActiva.getId());

        Factura factura = new Factura();
        factura.setNumero_factura("FAC-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" + generarNumeroFactura());
        factura.setFecha_pedido(LocalDateTime.now());
        factura.setMetodo_pago(metodoPago);
        factura.setCliente(usuario.getCliente());
        factura.setActivo(true);
        factura.setCaja(cajaActiva);
        log.info("Objeto Factura pre-creado con N°: {}", factura.getNumero_factura());

        List<Detalle_factura> detalles = new ArrayList<>();
        for (CarritoDTO item : itemsCarrito) {
            log.info("Procesando item: Producto ID {}, Cantidad: {}", item.getProductoId(), item.getCantidad());
            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + item.getProductoId()));

            log.info("Producto '{}' encontrado. Stock actual: {}", producto.getDescripcion(), producto.getStock());
            if (producto.getStock() < item.getCantidad()) {
                log.error("Stock insuficiente para producto: '{}'. Requerido: {}, Disponible: {}", producto.getDescripcion(), item.getCantidad(), producto.getStock());
                throw new StockInsuficienteException(
                        "Stock insuficiente para el producto: " + producto.getDescripcion());
            }

            int nuevoStock = producto.getStock() - item.getCantidad();
            log.info("Descontando stock para '{}'. Nuevo stock: {}", producto.getDescripcion(), nuevoStock);
            producto.setStock(nuevoStock);
            productoRepository.save(producto);

            Detalle_factura detalle = new Detalle_factura();
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecio_unitario(producto.getPrecio());
            detalle.setFactura(factura);
            detalles.add(detalle);
        }
        factura.setDetalles(detalles);

        log.info("Guardando factura y sus {} detalles...", detalles.size());
        Factura facturaGuardada = guardar(factura);
        log.info("Factura ID: {} guardada exitosamente en la base de datos.", facturaGuardada.getId());
        
        return facturaGuardada;
    }

    @Override
    @Transactional(readOnly = true)
    public String generarNumeroFactura() {
        Long ultimoNumero = facturaRepository.countByFechaPedidoBetween(
                LocalDateTime.now().toLocalDate().atStartOfDay(),
                LocalDateTime.now());
        return String.format("%04d", ultimoNumero + 1);
    }

    // Este método ya no es necesario, la lógica está en crearFacturaDesdeCarrito
    @Override
    public void actualizarInventario(List<CarritoDTO> itemsCarrito) {
        // Cuerpo vacío intencionalmente. La lógica fue centralizada.
    }
}