package com.analistas.luzclaritaweb.model.service.interfaces;
// import java.time.LocalDateTime;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.analistas.luzclaritaweb.model.domain.Factura;
import com.analistas.luzclaritaweb.model.domain.MovimientoCaja;
import com.analistas.luzclaritaweb.model.domain.Usuario;

public interface IMovimientoCajaService {
    List<MovimientoCaja> listarMovimientos();
    Optional<MovimientoCaja> obtenerMovimientoPorId(Long id);
    MovimientoCaja guardarMovimiento(MovimientoCaja movimiento);
    void eliminarMovimiento(Long id);

    // Métodos personalizados
    List<MovimientoCaja> buscarPorCaja(Long cajaId);
    List<MovimientoCaja> buscarPorTipoOperacion(MovimientoCaja.TipoOperacion tipo);
    List<MovimientoCaja> buscarPorRangoDeFechas(LocalDateTime inicio, LocalDateTime fin);
    List<MovimientoCaja> buscarPorOperador(Long operadorId);

    //Metodo para registra una venta online
    MovimientoCaja registrarIngresoPorVentaOnline(Factura factura, Usuario operador);

    // Registrar movimiento de caja (ingreso)
    MovimientoCaja registrarIngreso(MovimientoCaja movimiento, Usuario operador, Long cajaId, Factura factura);
   
    Map<String, Double> getResumenMovimientos(LocalDateTime desde, LocalDateTime hasta);
}
