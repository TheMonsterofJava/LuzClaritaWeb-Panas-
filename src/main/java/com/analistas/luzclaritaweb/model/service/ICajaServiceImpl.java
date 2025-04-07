package com.analistas.luzclaritaweb.model.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.analistas.luzclaritaweb.model.domain.Caja;
import com.analistas.luzclaritaweb.model.repository.ICajaRepository;

@Service
public class ICajaServiceImpl implements ICajaService {
    private final ICajaRepository cajaRepository;

    public ICajaServiceImpl(ICajaRepository cajaRepository) {
        this.cajaRepository = cajaRepository;
    }

    // public List<Caja> listarCajas() {
    //     return cajaRepository.findAll();
    // }

    @Override
    public Optional<Caja> obtenerCajaPorId(Long id) {
        return cajaRepository.findById(id);
    }

    @Override
    public Caja guardarCaja(Caja caja) {
        return cajaRepository.save(caja);
    }

    @Override
    public void eliminarCaja(Long id) {
        cajaRepository.deleteById(id);
    }

    // Métodos personalizados
    @Override
    public Optional<Caja> buscarUltimaCajaAbierta(Caja.EstadoCaja estado) {
        return cajaRepository.findTopByEstadoOrderByFechaDesc(estado);
    }

    @Override
    public List<Caja> buscarCajasPorUsuario(Long usuarioId) {
        return cajaRepository.findByUsuarioId(usuarioId);
    }

    @Override
    public List<Caja> buscarCajasPorRangoDeFechas(LocalDateTime inicio, LocalDateTime fin) {
        return cajaRepository.findByFechaBetween(inicio, fin);
    }

    @Override
    public List<Caja> listarCajasAbiertas() {
        return cajaRepository.findAll();
    }
}
