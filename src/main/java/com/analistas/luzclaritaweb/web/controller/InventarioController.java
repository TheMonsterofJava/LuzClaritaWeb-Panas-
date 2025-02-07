package com.analistas.luzclaritaweb.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/inventario")
@Controller
public class InventarioController {

    @GetMapping("/nuevo")
    public String nuevoInventario(Model model) {

        model.addAttribute("titulo", "Nuevo inventario");
        return "inventarios/form";
    }
}
