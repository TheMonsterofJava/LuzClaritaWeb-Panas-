package com.analistas.luzclaritaweb.model.service.interfaces;

import java.util.List;

import com.analistas.luzclaritaweb.model.domain.Compra;
import com.analistas.luzclaritaweb.model.domain.Proveedor;

public interface IProveedorService {

    public List <Proveedor> buscarTodo(); 

    public List <Proveedor> buscarPor(String criterio); 

    public List <Compra> getCompras();

    public Proveedor buscarPorId(Long id); 
    
    public void guardar(Proveedor proveedor); 

    public void agregar(); 

    public void borrarPorId(Long id); 

    //listar proveedores activos
    public List<Proveedor> listarProveedoresActivos();
}