package com.analistas.luzclaritaweb.model.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.analistas.luzclaritaweb.model.domain.Categoria;
import com.analistas.luzclaritaweb.model.domain.Producto;
import com.analistas.luzclaritaweb.model.repository.ICategoriaRepository;
import com.analistas.luzclaritaweb.model.repository.IProductoRepository;
import com.analistas.luzclaritaweb.model.service.interfaces.IProductoService;

@Service
public class ProductoServiceImpl implements IProductoService {

    // Inyección de dependencias
    @Autowired
    private IProductoRepository productoRepository;

    @Override
    @Transactional
    public void guardarProducto(Producto producto) { // Nombre corregido
        productoRepository.save(producto);
    }

    @Autowired
    ICategoriaRepository categoriaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Producto> buscarTodo() {
        return (List<Producto>) productoRepository.findAll();
    }

    @Override
     public List<Producto> buscar(Long categoriaId, String sortBy) {
        
        Sort sort;
        switch (sortBy) {
            case "precio_asc" -> sort = Sort.by("precio").ascending();
            case "precio_desc" -> sort = Sort.by("precio").descending();
            case "nombre_asc" -> sort = Sort.by("descripcion").ascending();
            case "nombre_desc" -> sort = Sort.by("descripcion").descending();
            default -> sort = Sort.by("precio").ascending();
        }

        if (categoriaId != null && categoriaId > 0) {
            return productoRepository.findByCategoriaId(categoriaId, sort);
        } else {
            return productoRepository.findAll(sort);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Producto buscarPorId(Long id) {
        return productoRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public Producto guardar(Producto producto) {
        return productoRepository.save(producto);
    }

    @Override
    public void borrarPorId(Long id) {
        productoRepository.deleteById(id);
    }

    @Override
    public List<Categoria> getCategorias() {

        return (List<Categoria>) categoriaRepository.findAll();

    }

     @Override
    @Transactional(readOnly = true)
    public List<Producto> buscarPorCategoria(Long categoriaId) {
        // Obtener todos los productos activos de la categoría
        List<Producto> productos = productoRepository.findByCategoriaIdAndActivoTrue(categoriaId);
        
        // Limitar a 4 productos aleatorios
        return productos.stream()
                .limit(4)
                .collect(Collectors.toList());
    }

    @Override
    public Producto buscarPorNombre(String descripcion) {
        return productoRepository.findByDescripcion(descripcion).orElse(null);

    }

}
