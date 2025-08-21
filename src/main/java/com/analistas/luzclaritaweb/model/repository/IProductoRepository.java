package com.analistas.luzclaritaweb.model.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import com.analistas.luzclaritaweb.model.domain.Producto;

public interface IProductoRepository extends JpaRepository<Producto, Long> {

    //select * from productos where activo = true;
    /* @Query("select p from Producto p where p.activo = true")
    List<Producto> buscarSoloActivos(); */

    //Con Query Methods
    List<Producto> findByActivoTrue();

    Optional<Producto> findByDescripcion(String descripcion);

    List<Producto> findByCategoriaId(Long categoriaId, Sort sort);

    List<Producto> findByCategoriaIdAndActivoTrue(Long categoriaId);


}
