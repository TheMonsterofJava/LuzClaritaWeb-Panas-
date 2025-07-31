package com.analistas.luzclaritaweb.web.controller;

import com.analistas.luzclaritaweb.model.domain.Receta;
import com.analistas.luzclaritaweb.model.service.interfaces.IRecetasService;

import jakarta.validation.Valid;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/recetas")
public class RecetaController {

    @Autowired
    private IRecetasService recetaService;

    // Listado de recetas (usa tipos.html)
    @GetMapping("/listado")
    public String listarRecetas(Model model) {
        model.addAttribute("titulo", "Recetas");
        model.addAttribute("recetas", recetaService.buscarTodo());
        model.addAttribute("titulo", "Listado de Recetas");
        return "recetas/listado";
    }

    // Formulario para nueva receta
    @GetMapping("/form")
    public String mostrarFormulario(Model model) {
        model.addAttribute("receta", new Receta());
        model.addAttribute("titulo", "Nueva Receta");
        return "recetas/form";
    }

    // Detalle de una receta (usa list.html)
    @GetMapping("/detalle/{id}")
    public String detalleReceta(@PathVariable Long id, Model model) {
        Receta receta = recetaService.buscarPorId(id);
        if (receta == null) {
            model.addAttribute("error", "Receta no encontrada");
            return "redirect:/recetas/listado";
        }
        model.addAttribute("receta", receta);
        model.addAttribute("titulo", receta.getNombre_receta());
        return "recetas/detalle";
    }

    @GetMapping("/cards")
public String mostrarCards(Model model) {
    model.addAttribute("titulo", "Recetas");
    model.addAttribute("recetas", recetaService.buscarTodo());
    return "recetas/cards";
}

    @PostMapping("/guardar")
public String guardarReceta(@Valid Receta receta, BindingResult result, Model model, SessionStatus status, RedirectAttributes flash) {

    if (result.hasErrors()) {
        model.addAttribute("error", "Corrija los errores en el formulario");
        return "recetas/form";
    }

    // Normalizar saltos de línea escritos como texto plano "\n"
    if (receta.getIngredientes() != null) {
        receta.setIngredientes(receta.getIngredientes().replace("\\n", "\n"));
    }
    if (receta.getPasos() != null) {
        receta.setPasos(receta.getPasos().replace("\\n", "\n"));
    }

    if (receta.getId() != null) {
        flash.addFlashAttribute("success", "Receta actualizada con éxito");
    } else {
        flash.addFlashAttribute("success", "Receta creada con éxito");
    }

    recetaService.guardar(receta);

    return "redirect:/recetas/listado";
}

@GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Long id, Model model) {

        Receta receta = recetaService.buscarPorId(id);

        model.addAttribute("titulo", "Editar Receta");
        model.addAttribute("receta", receta);

        return "recetas/form";
    }

    @GetMapping("/borrar/{id}")
    public String cambiarEstado(@PathVariable Long id, RedirectAttributes flash) {

        Receta receta = recetaService.buscarPorId(id);
        receta.setActivo(!receta.isActivo()); // Si está activo, lo desactivo y viceversa
        recetaService.guardar(receta);

        String mensaje = receta.isActivo() ? "registro de  " + receta.getNombre_receta() + "  habilitado"
                : "registro de  " + receta.getNombre_receta() + "  Deshabilitado";
        flash.addFlashAttribute("info", mensaje);

        return "redirect:/recetas/listado";
    }
}