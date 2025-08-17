package com.analistas.luzclaritaweb.model.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service; // Para idempotencia
import org.springframework.transaction.annotation.Transactional;

import com.analistas.luzclaritaweb.dto.CarritoDTO;
import com.analistas.luzclaritaweb.model.domain.Factura;
import com.analistas.luzclaritaweb.model.domain.Usuario;
import com.analistas.luzclaritaweb.model.repository.IFacturaRepository;
import com.analistas.luzclaritaweb.model.service.interfaces.ICarritoService;
import com.analistas.luzclaritaweb.model.service.interfaces.IFacturaService;
import com.analistas.luzclaritaweb.model.service.interfaces.IMovimientoCajaService;
import com.analistas.luzclaritaweb.model.service.interfaces.IProcesamientoPagoService;
import com.analistas.luzclaritaweb.web.excepciones.MontoDelPagoNoCoincideException;
import com.analistas.luzclaritaweb.web.excepciones.OrdenYaProcesadaException;
import com.analistas.luzclaritaweb.web.excepciones.PagoNoAprobadoException;
import com.analistas.luzclaritaweb.web.excepciones.StockInsuficienteException;

@Service
public class ProcesamientoPagoServiceImpl implements IProcesamientoPagoService {

    @Autowired
    private IFacturaService facturaService;

    @Autowired
    private IMovimientoCajaService movimientoCajaService;

    @Autowired
    private ICarritoService carritoService;
    
    @Autowired
    private IFacturaRepository facturaRepository; // Para la verificación de idempotencia

    // Considerar añadir IProductoService si la lógica de actualizar stock se mueve aquí directamente
    // o si se necesita para alguna validación adicional. Por ahora, IFacturaService.actualizarInventario lo maneja.

    @Autowired
    private com.analistas.luzclaritaweb.model.service.interfaces.IVentaService ventaService; // Inyectamos el servicio de Venta

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Factura procesarPagoExitosoMP(
            List<CarritoDTO> itemsCarrito,
            Usuario usuario,
            String metodoPago,
            String collectionId,
            String externalReference,
            BigDecimal montoPagadoEnMP,
            String estadoPagoMP)
            throws MontoDelPagoNoCoincideException, OrdenYaProcesadaException, StockInsuficienteException,
            PagoNoAprobadoException, Exception {

        // 0. Verificar estado del pago
        if (!"approved".equals(estadoPagoMP)) {
            throw new PagoNoAprobadoException(
                    "El pago con ID de colección " + collectionId + " no fue aprobado (" + estadoPagoMP + ").");
        }

        // 1. Idempotencia: Verificar si la orden ya fue procesada
        if (facturaRepository.existsByMpCollectionId(collectionId)) {
            throw new OrdenYaProcesadaException(
                    "La orden con ID de colección de MP " + collectionId + " ya fue procesada.");
        }

        // 2. Crear Factura (y RegistroVenta y actualizar stock dentro de este método)
        Factura factura = facturaService.crearFacturaDesdeCarrito(itemsCarrito, usuario, metodoPago);

        // Guardar el collectionId de MP en la factura para referencia futura e idempotencia
        factura.setMpCollectionId(collectionId);
        factura.setMpExternalReference(externalReference);
        facturaRepository.save(factura);

        // 3. Verificación de Monto
        BigDecimal totalCalculadoFactura = BigDecimal.valueOf(factura.calcularTotal());
        if (montoPagadoEnMP.compareTo(totalCalculadoFactura) != 0) {
            System.err.println("ALERTA: Monto de pago no coincide para Factura " + factura.getNumero_factura() +
                    ". Pagado MP: " + montoPagadoEnMP + ", Calculado: " + totalCalculadoFactura);
            throw new MontoDelPagoNoCoincideException(
                    "El monto pagado (" + montoPagadoEnMP + ") no coincide con el total de la factura ("
                            + totalCalculadoFactura + ")." +
                            " Factura Nro: " + factura.getNumero_factura() + ", MP Collection ID: " + collectionId);
        }

        // 4. Crear la Venta (sistema antiguo) a partir de la factura
        ventaService.crearVentaDesdeFactura(factura);

        // 5. Registrar Movimiento de Caja
        movimientoCajaService.registrarIngresoPorVentaOnline(factura, usuario);

        // 6. Vaciar Carrito
        carritoService.vaciarCarrito(usuario.getId());

        // La actualización de inventario ya no es necesaria aquí, porque se hace
        // dentro de crearFacturaDesdeCarrito
        // de forma atómica.

        return factura;
    }
}