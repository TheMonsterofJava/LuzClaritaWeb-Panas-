package com.analistas.luzclaritaweb.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import com.analistas.luzclaritaweb.model.service.InventarioService;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
@RequestMapping("/inventario")
public class InventarioController {

    @Autowired
    InventarioService inventarioService; 

    //Implemento el controlador del inventario
    @GetMapping("/listado")
    public String listar(Model model) {

        model.addAttribute("titulo", "listado de inventario");
        model.addAttribute("inventario", inventarioService.buscarTodo());
        
        return "inventario/list";
    }

}
