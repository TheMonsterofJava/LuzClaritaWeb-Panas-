package com.analistas.luzclaritaweb.model.repository;


import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.analistas.luzclaritaweb.model.domain.Categoria;

public interface ICategoriaRepository extends CrudRepository<Categoria, Long> {

    @Query("SELECT c FROM Categoria c LEFT JOIN c.productos p GROUP BY c.id ORDER BY COUNT(p) DESC")
    List<Categoria> findTopByProductos(Pageable pageable);
}