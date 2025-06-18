package com.analistas.luzclaritaweb.model.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.analistas.luzclaritaweb.model.domain.Categoria;

public interface ICategoriaRepository extends JpaRepository<Categoria, Long> {
    
}