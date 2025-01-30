package com.analistas.luzclaritaweb.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.analistas.luzclaritaweb.model.domain.Categoria;
import com.analistas.luzclaritaweb.model.domain.Producto;
import com.analistas.luzclaritaweb.model.service.IProductoService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;


@Controller
@RequestMapping("/productos")
@SessionAttributes("producto")
public class ProductoController {

    @Autowired
    IProductoService productoService;

    //Implementar el controlador de producto
    @GetMapping("/listado")
    public String listar(Model model) {

        model.addAttribute("titulo", "Productos");
        model.addAttribute("productos", productoService.buscarTodo());

        return "productos/list2";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {

        model.addAttribute("titulo", "Nuevo Producto");
        model.addAttribute("producto", new Producto());

        return "productos/form";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Long id, Model model) {

        Producto producto = productoService.buscarPorId(id);
        
        model.addAttribute("titulo", "Editar Producto");
        model.addAttribute("producto", producto);
        

        return "productos/form";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid Producto producto, BindingResult result,
        Model model, SessionStatus status, RedirectAttributes flash) {
        
            //Verificar si hay errores...
            if (result.hasErrors()) {
                model.addAttribute("error", "Corrija los errores...");
                return "productos/form";
            }

            // Verificar si el producto tiene ID (lo que indica que es un producto existente)
            boolean esNuevoProducto = (producto.getId() == null);

            productoService.guardar(producto);
            status.setComplete();

            // Determinar el mensaje y la clase del alert dependiendo si es un nuevo producto o uno editado
            String mensaje = esNuevoProducto ? 
            "Producto " + producto.getDescripcion() + " guardado con éxito" :
            "Producto " + producto.getDescripcion() + " modificado con éxito";

            String alertClass = esNuevoProducto ? "alert-success" : "alert-warning"; // Verde para nuevo, Naranja para editado

            // Agregar el mensaje y la clase para el alert
            flash.addFlashAttribute("info", mensaje); // Mensaje para mostrar
            flash.addFlashAttribute("alertClass", alertClass); // Clase para el alert (verde o naranja)

            return "redirect:/productos/listado"; // Redirigir al listado de productos
        }
    
    @GetMapping("/borrar/{id}")
    public String cambiarEstado(@PathVariable Long id, RedirectAttributes flash) {
        
        Producto producto = productoService.buscarPorId(id);
        producto.setActivo(!producto.isActivo()); //Si esta activo lo desactiva y viceversa
        productoService.guardar(producto);

        String mensaje = producto.isActivo() ? 
        "Producto " + producto.getDescripcion() + " habilitado" : "Producto " + producto.getDescripcion() + " deshabilitado";

        // Añadir clase al flash para determinar el color del alert
        String alertClass = producto.isActivo() ? "alert-info" : "alert-danger"; // Cambiar el color según el estado
        flash.addFlashAttribute("info", mensaje);
        flash.addFlashAttribute("alertClass", alertClass);

        return "redirect:/productos/listado";
    }
    

    @ModelAttribute("categorias")
    public List<Categoria> listarCategorias() {
        return productoService.getCategorias();
    }
}
