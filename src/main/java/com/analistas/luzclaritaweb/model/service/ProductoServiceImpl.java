package com.analistas.luzclaritaweb.model.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.analistas.luzclaritaweb.model.domain.Categoria;
import com.analistas.luzclaritaweb.model.domain.Producto;
import com.analistas.luzclaritaweb.model.repository.ICategoriaRepository;
import com.analistas.luzclaritaweb.model.repository.IProductoRepository;

@Service
public class ProductoServiceImpl implements IProductoService {

    @Autowired
    private IProductoRepository productoRepository;
    
    @Autowired
    private ICategoriaRepository categoriaRepository;

    @Override
    @Transactional
    public void guardarProducto(Producto producto) {
        productoRepository.save(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> buscarTodo() {
        return (List<Producto>) productoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> buscarActivos() {
        return productoRepository.findByActivoTrue();
    }

    @Override
    public List<Producto> buscarPor(String criterio) {
        // Implementación de búsqueda por criterio
        return productoRepository.findByNombreContainingOrDescripcionContaining(criterio, criterio);
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
    @Transactional
    public void borrarPorId(Long id) {
        productoRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Categoria> getCategorias() {
        return (List<Categoria>) categoriaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Producto buscarPorNombre(String nombre) {
        return productoRepository.findByNombre(nombre).orElse(null);
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
    @Transactional(readOnly = true)
    public List<Producto> buscarDestacados() {
        return productoRepository.findTop4ByActivoTrueOrderByPrecioDesc();
    }
}