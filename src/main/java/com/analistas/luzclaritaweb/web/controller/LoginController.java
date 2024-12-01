package com.analistas.luzclaritaweb.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 *
 * @author osval
 */

@Controller
public class LoginController {
    
    //Cargar formulario de inicio de sesion
    @GetMapping("/inicioSesion/login")
    public String mostrarLogin() {
    
        return "inicioSesion/login";
       
    }
    
    
    
}
