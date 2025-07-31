package com.analistas.luzclaritaweb.model.service.interfaces;
import  java.util.List;

import com.analistas.luzclaritaweb.model.domain.Inventario;
import com.analistas.luzclaritaweb.model.domain.Proveedor;
import com.analistas.luzclaritaweb.model.domain.Usuario;




public interface IInventarioService {

    public List <Inventario> buscarTodo(); 

    public List <Inventario> buscarPor(String criterio); 

    public List<Proveedor> getProveedores();

    public List<Usuario> getUsuarios();

    public Inventario buscarPorId(Long id); 
    
    public void guardar(Inventario inventario); 

    public void agregar(); 

    public void borrarPorId(Long id);

    public List<Inventario> buscarPorNombreParcial(String query); 
    
}
