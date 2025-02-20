package com.analistas.luzclaritaweb.model.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.analistas.luzclaritaweb.model.domain.Proveedor;

public interface IProveedorRepository extends CrudRepository<Proveedor, Long> {

    List<Proveedor> findByActivoTrue(); 
}
