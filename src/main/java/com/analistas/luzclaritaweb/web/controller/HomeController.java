package com.analistas.luzclaritaweb.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
    
@RequestMapping(path = {"/", "/home"}, method = RequestMethod.GET)
@Controller
public class HomeController {
    
    @GetMapping("/home")  
    public String home(Model model) {  
        model.addAttribute("urlcontacto", "/contacto"); 
        return "index"; 
    }
    
}