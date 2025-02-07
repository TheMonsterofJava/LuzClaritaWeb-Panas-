/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.analistas.luzclaritaweb.web.controller;

import com.analistas.luzclaritaweb.model.domain.Consulta;
import com.analistas.luzclaritaweb.model.service.IConsultaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/consultas")
public class ConsultasController {
    
    @Autowired
    private IConsultaService consultaService;

    @GetMapping("/consulta")
    public String mostrarFormulario(Model model) {
        model.addAttribute("consultas", new Consulta());
        return "consultas/consulta";
    }

    @PostMapping("/guardar")
    public String guardarConsulta(@ModelAttribute("consultas") Consulta consulta) {
        consultaService.guardarConsulta(consulta);
        //Antes return "redirect:/consulta?success"; // Redirige a una vista o confirma el envío
        return "redirect:/consultas/consulta?success"; //Cambios en el Redirect del guardado de la consulta por que entraba en conflicto con 
        //la clase de Web Config
    } 
}

