package com.analistas.luzclaritaweb.web.controller;

<<<<<<< HEAD
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
import com.analistas.luzclaritaweb.model.repository.ICategoriaRepository;
import com.analistas.luzclaritaweb.model.service.IProductoService;
=======
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
>>>>>>> 24fe31e4ab2a1ea9e2b801910c7c31c04c488572
    
@RequestMapping(path = {"/", "/home"}, method = RequestMethod.GET)
@Controller
public class HomeController {
    
<<<<<<< HEAD
    @Autowired
    private IProductoService productoService;
    ICategoriaRepository categoriaRepository;

    @GetMapping("/home")  
    public String home(Model model) {  
        // Obtiene la lista de productos desde la base de datos
        Iterable<Producto> productos = productoService.buscarTodo(); 
        
        // Agrega los productos al modelo
        model.addAttribute("productos", productos);
        
=======
    @GetMapping("/home")  
    public String home(Model model) {  
>>>>>>> 24fe31e4ab2a1ea9e2b801910c7c31c04c488572
        model.addAttribute("urlcontacto", "/contacto"); 
        return "index"; 
    }
    
<<<<<<< HEAD
    @ModelAttribute("categorias")
    public List<Categoria> listarCategorias() {
        return productoService.getCategorias();
    }
=======
>>>>>>> 24fe31e4ab2a1ea9e2b801910c7c31c04c488572
}