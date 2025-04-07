package com.analistas.luzclaritaweb.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.analistas.luzclaritaweb.model.domain.Inventario;
import com.analistas.luzclaritaweb.model.domain.Proveedor;
import com.analistas.luzclaritaweb.model.domain.Usuario;
import com.analistas.luzclaritaweb.model.service.IInventarioService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/inventario")
@SessionAttributes("inventario")
public class InventarioController {

    @Autowired
    IInventarioService inventarioService;

    // @Autowired
    // IProveedorRepository proveedorRepository;

    // @Autowired
    // IUsuarioRepository usuarioRepository;

    // Método para listar los productos
    @GetMapping("/listado")
    public String listar(Model model) {

        model.addAttribute("titulo", "Listado de Inventario");
        model.addAttribute("inventarioList", inventarioService.buscarTodo()); // Lista de productos

        return "inventario/list";
    }

    // Método para mostrar el formulario y guardar un nuevo producto

    @GetMapping("/nuevo")
    public String nuevo(Model model) {

        model.addAttribute("titulo", "Nuevo Ingrediente");
        model.addAttribute("inventario", new Inventario()); // Objeto vacío para el formulario

        return "inventario/form";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Long id, Model model) {

        Inventario inventario = inventarioService.buscarPorId(id);

        model.addAttribute("titulo", "Editar Inventario");
        model.addAttribute("inventario", inventario);

        return "inventario/form";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid Inventario inventario, BindingResult result,
            Model model, SessionStatus status) {

        // Verificar si hay errores...
        if (result.hasErrors()) {
            model.addAttribute("error", "corrija los errores");
            return "inventario/form";

        }

        inventarioService.guardar(inventario);
        status.setComplete();

        return "redirect:/inventario/listado";
    }

    @GetMapping("/borrar/{id}")
    public String cambiarEstado(@PathVariable Long id, RedirectAttributes flash) {

        Inventario inventario = inventarioService.buscarPorId(id);
        inventario.setActivo(!inventario.isActivo()); // Si está activo, lo desactivo y viceversa
        inventarioService.guardar(inventario);

        String mensaje = inventario.isActivo() ? "registro de  " + inventario.getNombreIngrediente() + "  habilitado"
                : "registro de  " + inventario.getNombreIngrediente() + "  Deshabilitado";
        flash.addFlashAttribute("info", mensaje);

        return "redirect:/inventario/listado";
    }

    // Cargar los proveedores y usuarios para usarlos en el formulario
    @ModelAttribute("proveedor")
    public List<Proveedor> listarProveedores() {
        return inventarioService.getProveedores();
    }

    @ModelAttribute("usuario")
    public List<Usuario> listarUsuarios() {
        return inventarioService.getUsuarios();
    }
}
