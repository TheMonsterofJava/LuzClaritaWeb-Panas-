package com.analistas.luzclaritaweb.model.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.analistas.luzclaritaweb.model.domain.Caja;
import com.analistas.luzclaritaweb.model.domain.Factura;
import com.analistas.luzclaritaweb.model.domain.MovimientoCaja;
import com.analistas.luzclaritaweb.model.domain.Usuario;
import com.analistas.luzclaritaweb.model.repository.IMovimientoCajaRepository;
import com.analistas.luzclaritaweb.model.service.interfaces.IMovimientoCajaService;
import com.analistas.luzclaritaweb.model.repository.ICajaRepository;

@Service
public class IMovimientoCajaServiceImpl implements IMovimientoCajaService {

    private final IMovimientoCajaRepository movimientoCajaRepository;
    private final ICajaRepository cajaRepository;

    public IMovimientoCajaServiceImpl(IMovimientoCajaRepository movimientoCajaRepository , ICajaRepository cajaRepository) {
        this.movimientoCajaRepository = movimientoCajaRepository;
        this.cajaRepository = cajaRepository;
    }

    @Override
    public List<MovimientoCaja> listarMovimientos() {
        return movimientoCajaRepository.findAll();
    }

    @Override
    public Optional<MovimientoCaja> obtenerMovimientoPorId(Long id) {
        return movimientoCajaRepository.findById(id);
    }

    @Override
    public MovimientoCaja guardarMovimiento(MovimientoCaja movimiento) {
        // Este método podría necesitar lógica transaccional si actualiza la caja también.
        // Por ahora, solo guarda el movimiento. La actualización de caja se hace en registrarIngresoPorVentaOnline.
        return movimientoCajaRepository.save(movimiento);
    }

    @Override
    public void eliminarMovimiento(Long id) {
        // Considerar si eliminar un movimiento debería ajustar el saldo de la caja.
        // Esto requeriría lógica transaccional y podría ser complejo.
        movimientoCajaRepository.deleteById(id);
    }

    // Métodos personalizados
    @Override
    public List<MovimientoCaja> buscarPorCaja(Long cajaId) {
        return movimientoCajaRepository.findByCajaId(cajaId);
    }

    @Override
    public List<MovimientoCaja> buscarPorTipoOperacion(MovimientoCaja.TipoOperacion tipo) {
        return movimientoCajaRepository.findByTipoOperacion(tipo);
    }

    @Override
    public List<MovimientoCaja> buscarPorRangoDeFechas(LocalDateTime inicio, LocalDateTime fin) {
        return movimientoCajaRepository.findByFechaBetween(inicio, fin);
    }

    @Override
    public List<MovimientoCaja> buscarPorOperador(Long operadorId) {
        return movimientoCajaRepository.findByOperadorId(operadorId);

    }

    // @Override
    // public MovimientoCaja registrarIngreso(MovimientoCaja movimiento, Usuario operador, Long cajaId, Factura factura) {
        
    //     throw new UnsupportedOperationException("Unimplemented method 'registrarIngreso'");
    // }

    //Metodo para Registrar una nueva venta online
    // Este método registra un ingreso de dinero en la caja por una venta online.
    //Este método es transaccional, lo que significa que si ocurre un error durante la ejecución,
    // todos los cambios realizados en la base de datos se revertirán automáticamente.
    @Override
    @org.springframework.transaction.annotation.Transactional // Asegurar transaccionalidad
    public MovimientoCaja registrarIngresoPorVentaOnline(Factura factura, Usuario operador) {
        if (factura == null) {
            throw new IllegalArgumentException("La factura no puede ser nula para registrar el ingreso.");
        }
        if (factura.getCaja() == null) {
            throw new IllegalStateException("La factura Nro: " + factura.getNumero_factura() + " no tiene una caja asignada. No se puede registrar el movimiento.");
        }

        Caja cajaDeLaFactura = factura.getCaja();

        MovimientoCaja movimiento = new MovimientoCaja();
        movimiento.setFecha(LocalDateTime.now());
        
        // Calcular el total de la factura. Asumimos que Detalle_factura.calcularSubtotal() devuelve Double.
        // Es más seguro si Factura.calcularTotal() devuelve BigDecimal.
        Double totalDouble = factura.calcularTotal(); 
        if (totalDouble == null) {
             throw new IllegalStateException("El total de la factura Nro: " + factura.getNumero_factura() + " no pudo ser calculado.");
        }
        BigDecimal montoTotal = BigDecimal.valueOf(totalDouble);

        movimiento.setMonto(montoTotal);
        movimiento.setMontoDouble(totalDouble);
        movimiento.setTipo("INGRESO");
        movimiento.setTipoOperacion(MovimientoCaja.TipoOperacion.INGRESO);
        movimiento.setDescripcion("Ingreso por Venta Online MP - Factura Nro: " + factura.getNumero_factura());
        movimiento.setCaja(cajaDeLaFactura);
        movimiento.setOperador(operador); // El usuario de la sesión (comprador) o un usuario sistema.
        movimiento.setFactura(factura); // Vincular con la factura

        MovimientoCaja guardado = movimientoCajaRepository.save(movimiento);

        // Actualizar saldo final de la caja
        // Es importante que la caja obtenida aquí sea la misma instancia o se refresque si es necesario.
        // Como la transacción debería englobar esto, la instancia debería estar gestionada por JPA.
        cajaDeLaFactura.setSaldoFinal(cajaDeLaFactura.getSaldoFinal().add(montoTotal));
        cajaRepository.save(cajaDeLaFactura); // Guardar la caja actualizada

        return guardado;
    }

    // Eliminar o comentar la vieja implementación de registrarIngreso si ya no se usa
    @Override
    public MovimientoCaja registrarIngreso(MovimientoCaja movimiento, Usuario operador, Long cajaId, Factura facturaInutilizada) {
        // Esta implementación ya no sería la principal para ventas online.
        // Se podría mantener para otros tipos de ingresos manuales si es necesario,
        // o refactorizarla para que use la nueva lógica si es apropiado.
        // Por ahora, la dejamos como estaba para no romper otras partes si existen.
        throw new UnsupportedOperationException("Utilice registrarIngresoPorVentaOnline para ventas de Mercado Pago.");
    }


    // Método para registrar un egreso
    // Sirve para registrar un egreso de dinero de la caja
    @Override
    public Map<String, Double> getResumenMovimientos(LocalDateTime desde, LocalDateTime hasta) {
        List<MovimientoCaja> movimientos = movimientoCajaRepository.findByFechaBetween(desde, hasta);
        
        // Corregir inicialización de totales para el resumen
        double totalIngresos = 0.0; // Iniciar en 0
        double totalEgresos = 0.0;  // Iniciar en 0

        //for para recorrer la lista de movimientos y calcular los totales
        // Se inicializa totalIngresos con un valor fijo para pruebas, luego se calculará dinámicamente
        for (MovimientoCaja m : movimientos) {
            if (m.getTipoOperacion() == MovimientoCaja.TipoOperacion.INGRESO) { // Usar Enum es más seguro
                totalIngresos += m.getMontoDouble();
            } else if (m.getTipoOperacion() == MovimientoCaja.TipoOperacion.EGRESO) { // Usar Enum
                totalEgresos += m.getMontoDouble();
            }
        }

        Map<String, Double> resumen = new HashMap<>();
        resumen.put("totalIngresos", totalIngresos);
        resumen.put("totalEgresos", totalEgresos);
        resumen.put("saldo", totalIngresos - totalEgresos);

        return resumen;
    }


}
