package com.analistas.luzclaritaweb.model.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.analistas.luzclaritaweb.model.domain.Inventario;
import com.analistas.luzclaritaweb.model.domain.Proveedor;
import com.analistas.luzclaritaweb.model.repository.IInventarioRepository;
import com.analistas.luzclaritaweb.model.repository.IProveedorRepository;

@Service
public class IInventarioServiceImpl implements InventarioService {

    @Autowired
    IInventarioRepository inventarioRepository; 

    @Autowired 
    IProveedorRepository proveedorRepository; 

    @Override
    @Transactional(readOnly = true)
    public List<Inventario> buscarTodo() {
        return (List<Inventario>) inventarioRepository.findAll();
    }

    @Override
    public List<Inventario> buscarPor(String criterio) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Inventario> buscarPor(Integer cantidad) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Transactional(readOnly = true)
    public Inventario buscarPorId(Long id) {
        return inventarioRepository.findById(id).orElse(null); 
    }

    @Override
    @Transactional
    public void guardar(Inventario inventario) {
        inventarioRepository.save(inventario);
    }
    

    @Override
    @Transactional
    public void borrarPorId(Long id) {
        inventarioRepository.deleteById(id);
    }

    @Override
    public void agregar() {
        throw new UnsupportedOperationException("Unimplemented method 'agregar'");
    }

    @Override
        public List<Proveedor> getProveedor() {

            return (List<Proveedor>) proveedorRepository.findAll();
        }

        @Override
        public List<Inventario> listar() {
           return buscarTodo();
        }

}
