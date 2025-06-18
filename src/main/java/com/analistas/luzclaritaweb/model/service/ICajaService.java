package com.analistas.luzclaritaweb.model.service;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.analistas.luzclaritaweb.model.domain.Caja;

public interface ICajaService {
    //List<Caja> listarCajas();
    Optional<Caja> obtenerCajaPorId(Long id);
    Caja guardarCaja(Caja caja);
    void eliminarCaja(Long id);

    // Métodos personalizados
    Optional<Caja> buscarUltimaCajaAbierta(Caja.EstadoCaja estado);
    List<Caja> buscarCajasPorUsuario(Long usuarioId);
    List<Caja> buscarCajasPorRangoDeFechas(LocalDateTime inicio, LocalDateTime fin);

    //listar Cajas Abiertas
    List<Caja> listarCajasAbiertas();
}
