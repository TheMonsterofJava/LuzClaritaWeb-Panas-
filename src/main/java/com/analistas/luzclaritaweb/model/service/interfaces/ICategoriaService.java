package com.analistas.luzclaritaweb.model.service.interfaces;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.analistas.luzclaritaweb.model.domain.Categoria;


public interface ICategoriaService {
    List<Categoria> buscarTodo();
    Categoria buscarPorId(Long id);
    void guardar(Categoria categoria);
    void eliminar(Long id);
    List<Categoria> buscarTopCategorias(Pageable pageable);
}
