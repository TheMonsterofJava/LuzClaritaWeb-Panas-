package com.analistas.luzclaritaweb.model.service;

import java.util.List;

import com.analistas.luzclaritaweb.model.domain.Receta;

public interface IRecetasService {
    List<Receta> buscarTodo();
    Receta guardar(Receta receta);
    Receta buscarPorId(Long id);
    void eliminar(Long id);
    List<Receta> buscarActivas();
}