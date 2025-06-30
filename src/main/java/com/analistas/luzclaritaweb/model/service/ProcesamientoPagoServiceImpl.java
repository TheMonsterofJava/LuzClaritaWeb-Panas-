package com.analistas.luzclaritaweb.model.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service; // Para idempotencia
import org.springframework.transaction.annotation.Transactional;

import com.analistas.luzclaritaweb.model.domain.Carrito;
import com.analistas.luzclaritaweb.model.domain.Factura;
import com.analistas.luzclaritaweb.model.domain.Usuario;
import com.analistas.luzclaritaweb.model.repository.IFacturaRepository;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Factura procesarPagoExitosoMP(
            List<Carrito> itemsCarrito,
            Usuario usuario,
            String metodoPago,
            String collectionId,
            String externalReference,
            BigDecimal montoPagadoEnMP,
            String estadoPagoMP)
            throws MontoDelPagoNoCoincideException, OrdenYaProcesadaException, StockInsuficienteException, PagoNoAprobadoException, Exception {

        // 0. Verificar estado del pago (aunque el controller ya lo hace, doble chequeo)
        if (!"approved".equals(estadoPagoMP)) {
            throw new PagoNoAprobadoException("El pago con ID de colección " + collectionId + " no fue aprobado (" + estadoPagoMP + ").");
        }

        // 1. (Punto 4e - Idempotencia) 
        //    Necesitaremos un campo en Factura para collectionId o usar externalReference si es único por intento de pago.
        //    Asumamos que Factura tendrá un campo mpCollectionId.
        if (facturaRepository.existsByMpCollectionId(collectionId)) { // Este método necesitará ser añadido a IFacturaRepository
             // Si ya existe, podríamos cargarla y devolverla, o lanzar la excepción.
             // Lanzar excepción es más claro para indicar que no se procesó de nuevo.
            throw new OrdenYaProcesadaException("La orden con ID de colección de MP " + collectionId + " ya fue procesada.");
        }
        // Opcionalmente, también verificar por externalReference si esa es la lógica deseada.
        // if (externalReference != null && facturaRepository.existsByMpExternalReference(externalReference)) {
        //     throw new OrdenYaProcesadaException("La orden con Referencia Externa " + externalReference + " ya fue procesada.");
        // }


        // 2. Crear Factura (IFacturaServiceImp ya asigna la Caja)
        Factura factura = facturaService.crearFacturaDesdeCarrito(itemsCarrito, usuario, metodoPago);
        
        // Guardar el collectionId de MP en la factura para referencia futura e idempotencia
        factura.setMpCollectionId(collectionId); // Necesitarás añadir este campo a la entidad Factura
        factura.setMpExternalReference(externalReference); // Y este también, si quieres guardar ambos
        // Luego guardar la factura con estos nuevos datos antes de proceder, o asegurar que `guardar` en facturaService los persista.
        // La forma más limpia es que crearFacturaDesdeCarrito devuelva la factura ya con todo, incluyendo estos IDs si se los pasas.
        // O hacer un facturaRepository.save(factura) aquí después de setearlos.
        // Por ahora, asumimos que se setean y se guardan. El método `guardar` dentro de `crearFacturaDesdeCarrito` ya es transaccional.
        // Para que sea más robusto, `crearFacturaDesdeCarrito` podría tomar `collectionId` y `externalReference` y guardarlos.
        // Vamos a optar por setearlos aquí y hacer un save explícito para claridad:
        facturaRepository.save(factura);


        // 3. (Punto 4d - Verificación de Monto)
        BigDecimal totalCalculadoFactura = BigDecimal.valueOf(factura.calcularTotal());
        // Usar compareTo para BigDecimal. Cuidado con la escala si los números vienen de diferentes fuentes.
        
        //Asignar un valor a totalCalculadoFactura para ver si salta la alerta 

        //Asignar el valor a totalCalculadoFactura para que salte la alerta
        //totalCalculadoFactura = new BigDecimal("100.00"); // Para pruebas,

        if (montoPagadoEnMP.compareTo(totalCalculadoFactura) != 0) {
            // Esto es un problema serio. El pago se hizo, pero el monto no coincide.
            // La transacción se revertirá. Se debe loguear este error detalladamente.
            System.err.println("ALERTA: Monto de pago no coincide para Factura " + factura.getNumero_factura() + 
                               ". Pagado MP: " + montoPagadoEnMP + ", Calculado: " + totalCalculadoFactura);
            throw new MontoDelPagoNoCoincideException(
                "El monto pagado (" + montoPagadoEnMP + ") no coincide con el total de la factura (" + totalCalculadoFactura + ")." +
                " Factura Nro: " + factura.getNumero_factura() + ", MP Collection ID: " + collectionId
            );
        }

        // 4. Registrar Movimiento de Caja
        // IMovimientoCajaServiceImpl.registrarIngresoPorVentaOnline ya es transaccional
        // y actualiza el saldo de la caja.
        movimientoCajaService.registrarIngresoPorVentaOnline(factura, usuario);

        // 5. Actualizar Inventario
        try {
            facturaService.actualizarInventario(itemsCarrito);
        } catch (RuntimeException e) { 
            // Podríamos tener una StockInsuficienteException específica lanzada desde actualizarInventario
            // y capturarla aquí para relanzarla como nuestra StockInsuficienteException del servicio.
            // Por ahora, si actualizarInventario lanza RuntimeException por stock, la transacción se revierte.
            // Si queremos un mensaje más específico, actualizarInventario debería lanzar StockInsuficienteException.
            System.err.println("Error de stock durante el procesamiento del pago para Factura " + factura.getNumero_factura() + ": " + e.getMessage());
            throw new StockInsuficienteException("Stock insuficiente al procesar la orden: " + e.getMessage());
        }
        
        // 6. Vaciar Carrito
        carritoService.vaciarCarrito(usuario.getId());

        return factura;
    }
}
