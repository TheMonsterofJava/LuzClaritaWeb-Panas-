package com.analistas.luzclaritaweb.model.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.analistas.luzclaritaweb.model.domain.Categoria;
import com.analistas.luzclaritaweb.model.domain.Producto;
import com.analistas.luzclaritaweb.model.repository.ICategoriaRepository;
import com.analistas.luzclaritaweb.model.repository.IProductoRepository;

@Service
public class ProductoServiceImpl implements IProductoService {

    //Inyección de dependencias
    @Autowired
    IProductoRepository productoRepository;

    @Autowired
    ICategoriaRepository categoriaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Producto> buscarTodo() {      
        return (List<Producto>) productoRepository.findAll();
    }

    @Override
    public List<Producto> buscarPor(String criterio) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'buscarPor'");
    }

    @Override
    @Transactional(readOnly = true)
    public Producto buscarPorId(Long id) {
        return productoRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void guardar(Producto producto) {
        productoRepository.save(producto);
    }

    @Override
    public void borrarPorId(Long id) {
        productoRepository.deleteById(id);
    }

    @Override
    public List<Categoria> getCategorias() {
    
        return (List<Categoria>) categoriaRepository.findAll();
    }

}
