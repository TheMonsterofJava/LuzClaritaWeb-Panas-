package com.analistas.luzclaritaweb.model.service;

import java.util.List;

import com.analistas.luzclaritaweb.model.domain.Categoria;


public interface ICategoriaService {
    List<Categoria> buscarTodo();
    Categoria buscarPorId(Long id);
    void guardar(Categoria categoria);
    void eliminar(Long id);
}
