package com.analistas.luzclaritaweb.model.repository;

import java.util.List;


import org.springframework.data.repository.CrudRepository;

import com.analistas.luzclaritaweb.model.domain.Inventario;

public interface IInventarioRepository extends CrudRepository<Inventario, Long> {

    List<Inventario> findByActivoTrue(); 
}
