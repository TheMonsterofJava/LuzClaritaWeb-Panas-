package com.analistas.luzclaritaweb.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.analistas.luzclaritaweb.model.domain.Producto;
import com.analistas.luzclaritaweb.model.service.IProductoService;
import com.analistas.luzclaritaweb.model.domain.Categoria;
import com.analistas.luzclaritaweb.model.repository.ICategoriaRepository;


    
@RequestMapping(path = {"/", "/home"}, method = RequestMethod.GET)
@Controller
public class HomeController {
    
    @Autowired
    private IProductoService productoService;
    
    @Autowired
    private ICategoriaRepository categoriaRepository;

    @GetMapping({"/", "/home"})
    public String home(Model model) {  
        // Obtiene la lista de productos desde la base de datos
        Iterable<Producto> productos = productoService.buscarTodo(); 
        
        // Agrega los productos al modelo
        model.addAttribute("productos", productos);

        Pageable topFive = PageRequest.of(0, 5);
        List<Categoria> categoriasTop = categoriaRepository.findTopByProductos(topFive);
        model.addAttribute("categoriasTop", categoriasTop);

        model.addAttribute("urlcontacto", "/contacto"); 
        return "index"; 
    }
    
    
}