package com.analistas.luzclaritaweb.model.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.analistas.luzclaritaweb.model.domain.RegistroVenta;
import com.analistas.luzclaritaweb.model.repository.IRegistroVentaRepository;
import com.analistas.luzclaritaweb.model.service.interfaces.IRegistroVentaService;

@Service
public class RegistroVentaServiceImpl implements IRegistroVentaService {

    @Autowired
    private IRegistroVentaRepository registroVentaRepository;

    @Override
    @Transactional
    public void registrarVenta(String descripcion, int cantidad, BigDecimal precioUnitario) {
        RegistroVenta registro = new RegistroVenta();
        
        registro.setFecha(LocalDateTime.now());
        registro.setDescripcionProducto(descripcion);
        registro.setCantidad(cantidad);
        registro.setPrecioUnitario(precioUnitario);
        
        BigDecimal total = precioUnitario.multiply(new BigDecimal(cantidad));
        registro.setTotalVenta(total);
        
        registroVentaRepository.save(registro);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistroVenta> buscarTodos() {
        // Devuelve los registros ordenados por fecha descendente para mostrar los más recientes primero
        return registroVentaRepository.findAll(Sort.by(Sort.Direction.DESC, "fecha"));
    }
}