package com.analistas.luzclaritaweb.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.analistas.luzclaritaweb.model.domain.Categoria;
import com.analistas.luzclaritaweb.model.domain.Producto;
import com.analistas.luzclaritaweb.model.service.ICategoriaService;
import com.analistas.luzclaritaweb.model.service.IProductoService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/productos")
@SessionAttributes("producto")
public class ProductoController {

    @Autowired
    IProductoService productoService;

    @Autowired
    ICategoriaService categoriaService;

    @GetMapping("/listado")
    public String listar(Model model) {
        model.addAttribute("titulo", "Productos");
        model.addAttribute("productos", productoService.buscarTodo());
        model.addAttribute("categorias", categoriaService.buscarTodo());
        model.addAttribute("categoria", new Categoria());
        return "productos/list";
    }

    @GetMapping("/listado2")
    public String listar2(Model model) {
        model.addAttribute("titulo", "Productos");
        model.addAttribute("productos", productoService.buscarTodo());
        model.addAttribute("categorias", categoriaService.buscarTodo());
        model.addAttribute("categoria", new Categoria());
        return "productos/list2";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("titulo", "Nuevo Producto");
        model.addAttribute("producto", new Producto());
        model.addAttribute("categorias", categoriaService.buscarTodo()); 
        return "productos/form";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Long id, Model model) {
        Producto producto = productoService.buscarPorId(id);
        model.addAttribute("titulo", "Editar Producto");
        model.addAttribute("producto", producto);
        model.addAttribute("categorias", categoriaService.buscarTodo()); 
        return "productos/form";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid Producto producto, BindingResult result,
        Model model, SessionStatus status, RedirectAttributes flash,
        @org.springframework.web.bind.annotation.RequestParam("imagen") MultipartFile imagen) {

        if (result.hasErrors()) {
            model.addAttribute("error", "Corrija los errores...");
            model.addAttribute("categorias", categoriaService.buscarTodo());
            return "productos/form";
        }

        // Guardar imagen si se subió
        if (!imagen.isEmpty()) {
            try {
                String carpeta = "uploads";
                String nombreArchivo = System.currentTimeMillis() + "_" + imagen.getOriginalFilename();
                Path ruta = Paths.get(carpeta).toAbsolutePath().resolve(nombreArchivo);
                Files.createDirectories(ruta.getParent());
                imagen.transferTo(ruta.toFile());
                producto.setLinkImagen("/" + carpeta + "/" + nombreArchivo);
            } catch (Exception e) {
                model.addAttribute("error", "Error al subir la imagen");
                model.addAttribute("categorias", categoriaService.buscarTodo());
                return "productos/form";
            }
        }

        boolean esNuevoProducto = (producto.getId() == null);
        productoService.guardar(producto);
        status.setComplete();

        String mensaje = esNuevoProducto ?
            "Producto '" + producto.getNombre() + "' guardado con éxito" :
            "Producto '" + producto.getNombre() + "' modificado con éxito";

        String alertClass = esNuevoProducto ? "alert-success" : "alert-warning";
        flash.addFlashAttribute("info", mensaje);
        flash.addFlashAttribute("alertClass", alertClass);

        return "redirect:/productos/listado2";
    }
    
    
    @GetMapping("/detalle/{id}")
    public String verDetalle(@PathVariable("id") Long id, Model model) {
        Producto producto = productoService.buscarPorId(id);
        List<Producto> productosRelacionados = productoService.buscarPorCategoria(producto.getCategoria().getId());
        
        model.addAttribute("producto", producto); // Aquí se pasa la descripción también
        model.addAttribute("productosRelacionados", productosRelacionados);
        model.addAttribute("titulo", "Detalle: " + producto.getNombre());

        return "productos/detalles";
    }

    @PostMapping("/categorias/guardar")
    public String guardarCategoria(@Valid Categoria categoria, BindingResult result, 
                                  RedirectAttributes flash, HttpServletRequest request) {
        if (result.hasErrors()) {
            flash.addFlashAttribute("error", "Corrija los errores...");
            return "redirect:" + request.getHeader("Referer");
        }
    
        categoriaService.guardar(categoria);
        flash.addFlashAttribute("info", "Categoría '" + categoria.getNombre() + "' guardada con éxito");
        return "redirect:" + request.getHeader("Referer");
    }

    // API endpoints
    @GetMapping("/api")
    @ResponseBody
    public List<Producto> listarApi() {
        return productoService.buscarTodo();
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public Producto detalleApi(@PathVariable Long id) {
        return productoService.buscarPorId(id);
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        productoService.borrarPorId(id);
        flash.addFlashAttribute("info", "Producto eliminado correctamente");
        flash.addFlashAttribute("alertClass", "alert-danger");
        return "redirect:/productos/listado2";
    }
}