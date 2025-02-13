package com.analistas.luzclaritaweb.model.repository;

import java.util.List;

/* import org.springframework.data.jpa.repository.Query; */
import org.springframework.data.repository.CrudRepository;

import com.analistas.luzclaritaweb.model.domain.Producto;

public interface IProductoRepository extends CrudRepository<Producto, Long> {

    //select * from productos where activo = true;
    /* @Query("select p from Producto p where p.activo = true")
    List<Producto> buscarSoloActivos(); */

    //Con Query Methods
    List<Producto> findByActivoTrue();
}
