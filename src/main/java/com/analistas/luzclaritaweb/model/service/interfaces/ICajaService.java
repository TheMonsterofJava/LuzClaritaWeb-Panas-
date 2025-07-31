package com.analistas.luzclaritaweb.model.service.interfaces;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.analistas.luzclaritaweb.model.domain.Caja;

public interface ICajaService {
    List<Caja> listarCajas(); // Impl will use findByActivaTrue
    Optional<Caja> obtenerCajaPorId(Long id); // Impl will use findByIdAndActivaTrue
    Caja guardarCaja(Caja caja);
    void desactivarCaja(Long id); // Renamed from eliminarCaja

    // Métodos personalizados
    Optional<Caja> buscarUltimaCajaAbiertaYActiva(Caja.EstadoCaja estado); // Renamed and impl will use new repo method
    List<Caja> buscarCajasPorUsuario(Long usuarioId); // No change in signature for now
    List<Caja> buscarCajasPorRangoDeFechas(LocalDateTime inicio, LocalDateTime fin); // No change for now

    //listar Cajas Abiertas
    List<Caja> listarCajasAbiertas(); // Impl will use findByEstadoAndActivaTrue
}

