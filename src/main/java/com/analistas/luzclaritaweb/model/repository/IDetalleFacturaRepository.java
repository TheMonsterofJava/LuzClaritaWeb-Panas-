package com.analistas.luzclaritaweb.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.analistas.luzclaritaweb.model.domain.Detalle_factura;
import com.analistas.luzclaritaweb.model.domain.Factura;

@Repository
public interface IDetalleFacturaRepository extends JpaRepository<Detalle_factura, Long> {
    List<Detalle_factura> findByFactura(Factura factura);
}

