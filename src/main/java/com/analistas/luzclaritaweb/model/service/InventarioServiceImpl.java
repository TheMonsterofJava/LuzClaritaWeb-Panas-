package com.analistas.luzclaritaweb.model.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.analistas.luzclaritaweb.model.domain.Inventario;
import com.analistas.luzclaritaweb.model.domain.Proveedor;
import com.analistas.luzclaritaweb.model.domain.Usuario;
import com.analistas.luzclaritaweb.model.repository.IInventarioRepository;
import com.analistas.luzclaritaweb.model.repository.IProveedorRepository;
import com.analistas.luzclaritaweb.model.repository.IUsuarioRepository;

@Service
public class InventarioServiceImpl implements IInventarioService {

    @Autowired
    IInventarioRepository inventarioRepository; 

    @Autowired 
    IProveedorRepository proveedorRepository;   

    @Autowired 
    IUsuarioRepository usuarioRepository;   
    
    @Override
    @Transactional(readOnly = true)
    public List<Inventario> buscarTodo() {
        return (List<Inventario>) inventarioRepository.buscarSoloActivos();
    }

    @Override
    public List<Inventario> buscarPor(String criterio) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Transactional
    public void guardar(Inventario inventario) {
        inventarioRepository.save(inventario);
    }

    @Override
    public void agregar() {
        throw new UnsupportedOperationException("Unimplemented method 'agregar'");
    }

    @Override
        public List<Proveedor> getProveedores() {
            return (List<Proveedor>) proveedorRepository.findAll();
        }

    @Override
    public List<Usuario> getUsuarios() {
        return (List<Usuario>) usuarioRepository.findAll();
    }

    @Override
    public Inventario buscarPorId(Long id) {
    return inventarioRepository.findById(id).orElse(null);
    }

    @Override
    public void borrarPorId(Long id) {
        inventarioRepository.deleteById(id);
    }

    @Override
    public List<Inventario> buscarPorNombreParcial(String q) {
    return inventarioRepository.findByNombreIngredienteContainingIgnoreCase(q);
}

}

        

