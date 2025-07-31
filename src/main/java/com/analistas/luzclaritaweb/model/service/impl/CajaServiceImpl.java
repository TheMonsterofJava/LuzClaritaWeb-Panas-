package com.analistas.luzclaritaweb.model.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.analistas.luzclaritaweb.model.domain.Caja;
import com.analistas.luzclaritaweb.model.repository.ICajaRepository;
import com.analistas.luzclaritaweb.model.service.interfaces.ICajaService;

@Service
public class CajaServiceImpl implements ICajaService {
    private final ICajaRepository cajaRepository;

    public CajaServiceImpl(ICajaRepository cajaRepository) {
        this.cajaRepository = cajaRepository;
    }

    @Override
    public List<Caja> listarCajas() {
        return cajaRepository.findByActivaTrue();
    }

    @Override
    public Optional<Caja> obtenerCajaPorId(Long id) {
        return cajaRepository.findByIdAndActivaTrue(id);
    }

    @Override
    public Caja guardarCaja(Caja caja) {
        return cajaRepository.save(caja);
    }

    @Override
    public void desactivarCaja(Long id) {
        Optional<Caja> cajaOpt = cajaRepository.findById(id); // Find regardless of active status
        if (cajaOpt.isPresent()) {
            Caja caja = cajaOpt.get();
            caja.setActiva(false);
            cajaRepository.save(caja);
        }
        // Consider logging if caja is not found, or throwing an exception
    }

    // Métodos personalizados
    @Override
    public Optional<Caja> buscarUltimaCajaAbiertaYActiva(Caja.EstadoCaja estado) {
        // Ensure 'estado' is typically Caja.EstadoCaja.ABIERTA when calling this
        return cajaRepository.findTopByEstadoAndActivaTrueOrderByFechaDesc(estado);
    }

    // Métodos de búsqueda personalizados
    @Override
    public List<Caja> buscarCajasPorUsuario(Long usuarioId) {
        return cajaRepository.findByUsuarioId(usuarioId);
    }

    // Método para buscar cajas por rango de fechas
    @Override
    public List<Caja> buscarCajasPorRangoDeFechas(LocalDateTime inicio, LocalDateTime fin) {
        return cajaRepository.findByFechaBetween(inicio, fin);
    }

    // Método para listar cajas abiertas
    @Override
    public List<Caja> listarCajasAbiertas() {
        return cajaRepository.findByEstadoAndActivaTrue(Caja.EstadoCaja.ABIERTA);
    }
}
