package com.analistas.luzclaritaweb.model.service;

import java.util.List;

import com.analistas.luzclaritaweb.model.domain.Categoria;
import com.analistas.luzclaritaweb.model.domain.Producto;

public interface IProductoService {

    public List<Producto> buscarTodo();

    public List<Producto> buscarPor(String criterio);

    public Producto buscarPorId(Long id);

    public void guardar(Producto producto);

    public void borrarPorId(Long id);

    public List<Categoria> getCategorias();
}
