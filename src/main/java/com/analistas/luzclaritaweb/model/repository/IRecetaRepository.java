/*package com.analistas.luzclaritaweb.model.repository;

import com.analistas.luzclaritaweb.model.domain.Receta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface IRecetaRepository extends JpaRepository<Receta, Long> {

    @Query("select r from Receta r where r.nombreReceta like %?1%")
    List<Receta> buscarPorNombre(String nombre);
    
    List<Receta> findByNombreRecetaContainingIgnoreCase(String nombre);
}*/