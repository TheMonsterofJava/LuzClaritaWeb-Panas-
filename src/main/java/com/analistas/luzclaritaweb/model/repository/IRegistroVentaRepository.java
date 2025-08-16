package com.analistas.luzclaritaweb.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.analistas.luzclaritaweb.model.domain.RegistroVenta;

public interface IRegistroVentaRepository extends JpaRepository<RegistroVenta, Long> {
    
}