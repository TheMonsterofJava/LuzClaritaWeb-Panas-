package com.analistas.luzclaritaweb.model.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.analistas.luzclaritaweb.model.domain.Factura;
import com.analistas.luzclaritaweb.model.domain.MovimientoCaja;
import com.analistas.luzclaritaweb.model.domain.Usuario;
import com.analistas.luzclaritaweb.model.repository.IMovimientoCajaRepository;

@Service
public class IMovimientoCajaServiceImpl implements IMovimientoCajaService {
    private final IMovimientoCajaRepository movimientoCajaRepository;

    public IMovimientoCajaServiceImpl(IMovimientoCajaRepository movimientoCajaRepository) {
        this.movimientoCajaRepository = movimientoCajaRepository;
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
        return movimientoCajaRepository.save(movimiento);
    }

    @Override
    public void eliminarMovimiento(Long id) {
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

    @Override
    public MovimientoCaja registrarIngreso(MovimientoCaja movimiento, Usuario operador, Long cajaId, Factura factura) {
        
        throw new UnsupportedOperationException("Unimplemented method 'registrarIngreso'");
    }


}
