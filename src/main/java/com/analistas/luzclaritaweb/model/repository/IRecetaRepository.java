package com.analistas.luzclaritaweb.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.analistas.luzclaritaweb.model.domain.Receta;

public interface IRecetaRepository extends JpaRepository<Receta, Long> {
    List<Receta> findByActivoTrue();

    @Modifying
    @Query("UPDATE Receta r SET r.activo = :activo WHERE r.id = :id")
    void setActivo(@Param("id") Long id, @Param("activo") boolean activo);

}