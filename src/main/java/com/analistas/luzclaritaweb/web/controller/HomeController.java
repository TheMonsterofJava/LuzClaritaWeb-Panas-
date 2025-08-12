package com.analistas.luzclaritaweb.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.analistas.luzclaritaweb.model.domain.Categoria;
import com.analistas.luzclaritaweb.model.domain.Producto;
import com.analistas.luzclaritaweb.model.service.interfaces.IProductoService;


    
@RequestMapping(path = {"/", "/home"}, method = RequestMethod.GET)
@Controller
public class HomeController {
    
    @Autowired
    private IProductoService productoService;

    // He combinado tus dos métodos en uno solo que responde a "/", "/home" e "/index".
    @GetMapping({"/", "/home", "/index"})  
    public String mostrarHomePage(Model model) {  
        // Llama al nuevo método 'buscar' sin filtro de categoría (null) 
        // y con un orden por defecto ("precio_asc").
        List<Producto> productos = productoService.buscar(null, "precio_asc"); 
        
        // Agrega los productos al modelo
        model.addAttribute("productos", productos);
        
        model.addAttribute("urlcontacto", "/contacto"); 
        return "index"; 
    }
    
    @ModelAttribute("categorias")
    public List<Categoria> listarCategorias() {
        // Esto sigue funcionando igual, ya que no tocamos el servicio de categorías.
        return productoService.getCategorias();
    }
    
    //Controlar la vista de error 403
    @GetMapping("/accessDenied")
    public String accessDenied() {
        return "accessDenied";
    }

}