package com.analistas.luzclaritaweb.model.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;

import com.analistas.luzclaritaweb.model.domain.Compra;
import com.analistas.luzclaritaweb.model.repository.ICompraRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class ICompraServiceImpl implements ICompraService {

    private final ICompraRepository compraRepository;

    public ICompraServiceImpl(ICompraRepository compraRepository) {
        this.compraRepository = compraRepository;
    }

    @Override
    public List<Compra> listarCompras() {
        return compraRepository.findAll();
    }

    @Override
    public Optional<Compra> obtenerCompraPorId(Long id) {
        return compraRepository.findById(id);
    }

    @Override
    @Transactional
    public Compra guardarCompra(Compra compra) {
        return compraRepository.save(compra); // Cascade.ALL se encargará de los detalles
    }

    @Override
    public void eliminarCompra(Long id) {
        compraRepository.deleteById(id);
    }

    @Override
    public List<Compra> buscarPorRangoDeFechas(LocalDateTime inicio, LocalDateTime fin) {
        return compraRepository.findByFechaHoraBetween(inicio, fin);
    }

    @Override
    public List<Compra> buscarPorUsuario(Long usuarioId) {
        return compraRepository.findByUsuarioId(usuarioId);
    }

    @Override
    public List<Compra> buscarPorProveedor(Long proveedorId) {
        return compraRepository.findByProveedorId(proveedorId);
    }

    @Override
    public List<Compra> buscarPorCaja(Long cajaId) {
        return compraRepository.findByCajaId(cajaId);
    }

    @Override
    public List<Compra> listarComprasActivas() {
        return compraRepository.findByActivoTrue();
    }

    @Override
    public Optional<Compra> buscarPorId(Long id) {
        return compraRepository.findById(id);
    }

    @Override
    public Compra buscarPorIdConDetalles(Long id) {
        return compraRepository.findById(id)
                .map(compra -> {
                    // Inicializar colecciones necesarias
                    Hibernate.initialize(compra.getDetalles());
                    if (compra.getDetalles() != null) {
                        compra.getDetalles().forEach(detalle -> {
                            Hibernate.initialize(detalle.getProducto());
                        });
                    }
                    return compra;
                })
                .orElseThrow(() -> new EntityNotFoundException("Compra no encontrada"));
    }

}