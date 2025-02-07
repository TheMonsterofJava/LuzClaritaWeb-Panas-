package com.analistas.luzclaritaweb.model.repository;

import org.springframework.data.repository.CrudRepository;

import com.analistas.luzclaritaweb.model.domain.Categoria;

public interface ICategoriaRepository extends CrudRepository<Categoria, Long> {
    
}