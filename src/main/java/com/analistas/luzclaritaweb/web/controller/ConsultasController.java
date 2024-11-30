/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.analistas.luzclaritaweb.web.controller;
import com.analistas.luzclaritaweb.model.domain.Consulta;



/**
 *
 * @author osval
 */
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
public class ConsultasController {

    @GetMapping("/consulta")
    public String mostrarFormulario(Model model) {
        model.addAttribute("consultas", new Consulta()); // Asegura que este objeto esté en el modelo
        return "consultas/consulta"; // Nombre de la plantilla
    }

    @PostMapping("/guardar")
    public String guardarConsulta(@ModelAttribute("consultas") Consulta consulta) {
        // Lógica para guardar los datos (puedes usar un servicio)
        return "redirect:/consulta"; // Redirige a una vista o confirma el envío
    }
}



//@Controller
//@RequestMapping("/consultas")
//public class ConsultasController {
//
//    //Pagina Principal
//    @GetMapping("/consulta")
//    public String consulta(Model model) {
//    
//        model.addAttribute("titulo", "Consultas");
//        return "consultas/consulta";
//    
//    }
//            
// 
//}