package com.analistas.luzclaritaweb.model.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.analistas.luzclaritaweb.model.domain.DetalleCompra;
import com.analistas.luzclaritaweb.model.repository.IDetalleCompraRepository;
import com.analistas.luzclaritaweb.model.service.interfaces.IDetalleCompraService;


@Service
public class DetalleCompraServiceImpl implements IDetalleCompraService {

    @Autowired
    private final IDetalleCompraRepository detalleCompraRepository;

    public DetalleCompraServiceImpl(IDetalleCompraRepository detalleCompraRepository) {
        this.detalleCompraRepository = detalleCompraRepository;
    }

    @Override
    public List<DetalleCompra> listarDetallesCompra() {
        return detalleCompraRepository.findAll();
    }

    @Override
    public Optional<DetalleCompra> obtenerDetallePorId(Long id) {
        return detalleCompraRepository.findById(id);
    }

    @Override
    public List<DetalleCompra> obtenerDetallesPorCompra(Long compraId) {
        return detalleCompraRepository.findByCompraId(compraId);
    }

    @Override
    public DetalleCompra guardarDetalleCompra(DetalleCompra detalleCompra) {
        return detalleCompraRepository.save(detalleCompra);
    }

    @Override
    public void eliminarDetalleCompra(Long id) {
        detalleCompraRepository.deleteById(id);
    }

}
