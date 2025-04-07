package com.analistas.luzclaritaweb.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/recetas")
public class RecetaController {

    //Implementar el controlados de recetas
    @GetMapping("/clasicas")
    public String recetaClasica(Model model) {
        
        model.addAttribute("titulo", "Recetas clasicas");

        return "receta-clasica/list";
    }

    @GetMapping("/especiales")
    public String recetaEspecial(Model model) {

        model.addAttribute("titulo", "Recetas especiales");

        return "receta-especial/list";
    }

    @RequestMapping("/consejos")
    public String consejosCocina(Model model) {

        model.addAttribute("titulo", "Consejos de cocina");

        return "consejos/list";
    }
}
