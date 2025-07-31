package com.analistas.luzclaritaweb.model.service.interfaces;

import java.util.List;

import com.analistas.luzclaritaweb.model.domain.Categoria;
import com.analistas.luzclaritaweb.model.domain.Producto;


public interface IProductoService {

    public List<Producto> buscarTodo();

    public List<Producto> buscarPor(String criterio);

    public Producto buscarPorId(Long id);

    Producto guardar(Producto producto);

    public void borrarPorId(Long id);

    public List<Categoria> getCategorias();

    void guardarProducto(Producto producto);

    Producto buscarPorNombre(String nombre);
}
