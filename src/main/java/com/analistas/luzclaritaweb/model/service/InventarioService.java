package com.analistas.luzclaritaweb.model.service;
import  java.util.List;

import com.analistas.luzclaritaweb.model.domain.Inventario;
import com.analistas.luzclaritaweb.model.domain.Proveedor;

public interface InventarioService {

    public List <Inventario> buscarTodo(); 

    public List <Inventario> buscarPor(String criterio); 

    public List <Inventario> buscarPor(Integer cantidad);
    
    public List<Proveedor> getProveedor();

    public Inventario buscarPorId(Long id); 
    
    public void guardar(Inventario inventario); 

    public void agregar();
    
    public void borrarPorId(Long Id);

    public List<Inventario> listar();

}
