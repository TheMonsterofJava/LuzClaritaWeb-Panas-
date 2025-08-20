package com.analistas.luzclaritaweb.model.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.analistas.luzclaritaweb.model.domain.Caja;
import com.analistas.luzclaritaweb.model.domain.Caja.TipoCaja;


@Repository
public interface ICajaRepository extends JpaRepository<Caja, Long> {

    // Buscar la última caja abierta
    Optional<Caja> findTopByEstadoOrderByFechaDesc(Caja.EstadoCaja estado);

    // Buscar cajas de un usuario en particular
    List<Caja> findByUsuarioId(Long usuarioId);

    // Buscar cajas dentro de un rango de fechas
    List<Caja> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);

    List<Caja> findByEstado(Caja.EstadoCaja estado);

    // Filtrar por estado y activa
    // Borrados lógicos:
    // findByActivaTrue(): Obtiene todas las cajas activas.
    // findByEstadoAndActivaTrue(EstadoCaja estado): Obtiene las cajas activas por estado.
    // findByIdAndActivaTrue(Long id): Obtiene una caja por ID si está activa.
    // findTopByEstadoAndActivaTrueOrderByFechaDesc(EstadoCaja estado): Obtiene la última caja abierta activa.
    List<Caja> findByActivaTrue();
    List<Caja> findByEstadoAndActivaTrue(Caja.EstadoCaja estado);
    Optional<Caja> findByIdAndActivaTrue(Long id);
    Optional<Caja> findTopByEstadoAndActivaTrueOrderByFechaDesc(Caja.EstadoCaja estado);
    Optional<Caja> findTopByTipoCajaAndEstadoAndActivaTrueOrderByFechaDesc(TipoCaja tipoCaja, Caja.EstadoCaja estado);
}


// 📝 Explicación:

// findTopByEstadoOrderByFechaDesc(EstadoCaja estado): Obtiene la última caja abierta.

// findByUsuarioId(Long usuarioId): Filtra las cajas por usuario.

// findByFechaBetween(LocalDateTime inicio, LocalDateTime fin): Obtiene las cajas de un periodo determinado.