package com.analistas.luzclaritaweb.model.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.analistas.luzclaritaweb.model.domain.Producto;

public interface IProductoRepository extends CrudRepository<Producto, Long> {

    //select * from productos where activo = true;
    /* @Query("select p from Producto p where p.activo = true")
    List<Producto> buscarSoloActivos(); */

    //Con Query Methods
    Optional<Producto> findByNombre(String nombre);
    List<Producto> findByNombreContainingOrDescripcionContaining(String nombre, String descripcion);
    List<Producto> findByActivoTrue();
    List<Producto> findByCategoriaIdAndActivoTrue(Long categoriaId);
    List<Producto> findTop4ByActivoTrueOrderByPrecioDesc();
    Optional<Producto> findByDescripcion(String descripcion);

}
