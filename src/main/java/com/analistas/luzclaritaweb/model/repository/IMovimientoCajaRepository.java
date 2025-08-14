package com.analistas.luzclaritaweb.model.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.analistas.luzclaritaweb.model.domain.MovimientoCaja;

@Repository
public interface IMovimientoCajaRepository extends JpaRepository<MovimientoCaja, Long> {

    // Buscar todos los movimientos de una caja en particular
    List<MovimientoCaja> findByCajaId(Long cajaId);

    // Buscar movimientos por tipo (Ingreso o Egreso)
    List<MovimientoCaja> findByTipoOperacion(MovimientoCaja.TipoOperacion tipo);

    // Buscar movimientos en un rango de fechas
    List<MovimientoCaja> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);

    // Buscar movimientos de caja realizados por un usuario
    List<MovimientoCaja> findByOperadorId(Long operadorId);

    public void deleteByCompraId(Long compraId);

    MovimientoCaja findByCompraId(Long compraId);

}
