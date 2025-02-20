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
import com.analistas.luzclaritaweb.model.service.IProductoService;
    
@RequestMapping(path = {"/", "/home"}, method = RequestMethod.GET)
@Controller
public class HomeController {
    
    @Autowired
    private IProductoService productoService;
    //ICategoriaRepository categoriaRepository;

    @GetMapping("/home")  
    public String home(Model model) {  
        // Obtiene la lista de productos desde la base de datos
        Iterable<Producto> productos = productoService.buscarTodo(); 
        
        // Agrega los productos al modelo
        model.addAttribute("productos", productos);
        
        model.addAttribute("urlcontacto", "/contacto"); 
        return "index"; 
    }
    
    @ModelAttribute("categorias")
    public List<Categoria> listarCategorias() {
        return productoService.getCategorias();
    }
}