package com.analistas.luzclaritaweb.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.analistas.luzclaritaweb.model.domain.Receta;

public interface IRecetaRepository extends JpaRepository<Receta, Long> {
    List<Receta> findByActivoTrue();
}