package com.analistas.luzclaritaweb.model.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.analistas.luzclaritaweb.model.domain.Caja;

@Repository
public interface ICajaRepository extends JpaRepository<Caja, Long> {

    // Buscar la última caja abierta
    Optional<Caja> findTopByEstadoOrderByFechaDesc(Caja.EstadoCaja estado);

    // Buscar cajas de un usuario en particular
    List<Caja> findByUsuarioId(Long usuarioId);

    // Buscar cajas dentro de un rango de fechas
    List<Caja> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);
}
