package com.analistas.luzclaritaweb.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.analistas.luzclaritaweb.model.domain.Categoria;
import com.analistas.luzclaritaweb.model.repository.ICategoriaRepository;

@Controller
public class CategoriaController {

    @Autowired
    private ICategoriaRepository categoriaRepository;

    @GetMapping("/categorias")
    public String verCategorias(Model model) {
        List<Categoria> categorias = categoriaRepository.findAll();
        model.addAttribute("categorias", categorias);
        model.addAttribute("categoria", new Categoria()); // Para el formulario de la categoría
        return "categorias"; // Página donde se muestran las categorías y el formulario
    }

    // Método para agregar una nueva categoría
    @PostMapping("/categorias/guardar")
    public String agregarCategoria(@ModelAttribute Categoria categoria, RedirectAttributes redirectAttributes) {
        categoriaRepository.save(categoria);
        redirectAttributes.addFlashAttribute("mensajeExito", "Categoría agregada correctamente");
        return "redirect:/productos/listado2";  // Redirige a la página de listado de productos
    }

    // Método para eliminar una categoría
    @PostMapping("/categorias/eliminar")
    public String eliminarCategoria(@RequestParam("categoriaId") Long categoriaId, RedirectAttributes redirectAttributes) {
        if (categoriaId != null) {
            categoriaRepository.deleteById(categoriaId); // Eliminar la categoría de la base de datos
            redirectAttributes.addFlashAttribute("mensajeEliminar", "Categoría eliminada correctamente");
        } else {
            redirectAttributes.addFlashAttribute("mensajeError", "No se seleccionó ninguna categoría.");
        }
        return "redirect:/productos/listado2";
    }
}
