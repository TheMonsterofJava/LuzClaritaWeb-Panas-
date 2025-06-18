package com.analistas.luzclaritaweb.model.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.analistas.luzclaritaweb.model.domain.Compra;
import com.analistas.luzclaritaweb.model.domain.Proveedor;
import com.analistas.luzclaritaweb.model.repository.ICompraRepository;
import com.analistas.luzclaritaweb.model.repository.IProveedorRepository;

@Service
public class ProveedorServiceImpl implements IProveedorService {

    @Autowired
    IProveedorRepository proveedorRepository; 

    @Autowired
    ICompraRepository compraRepository; 

    @Override
    @Transactional
    public List<Proveedor> buscarTodo() {
        return (List<Proveedor>) proveedorRepository.findAll();
    }

    @Override
    public List<Proveedor> buscarPor(String criterio) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Compra> getCompras() {
        return (List<Compra>) compraRepository.findAll(); 
    }

    @Override
    public Proveedor buscarPorId(Long id) {
        return proveedorRepository.findById(id).orElse(null); 
    }

    @Override
    @Transactional
    public void guardar(Proveedor proveedor) {
        proveedorRepository.save(proveedor); 
    }

    @Override
    public void agregar() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void borrarPorId(Long id) {
        proveedorRepository.deleteById(id);
    }

    @Override
    public List<Proveedor> listarProveedoresActivos() {
        return (List<Proveedor>) proveedorRepository.findAll();
    }    

    
    
}
