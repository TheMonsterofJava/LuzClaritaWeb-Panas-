package com.analistas.luzclaritaweb.web.controller;

import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.analistas.luzclaritaweb.model.domain.*;
import com.analistas.luzclaritaweb.model.service.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/productos")
@SessionAttributes("producto")
public class ProductoController {

    private final Logger logger = LoggerFactory.getLogger(ProductoController.class);
    
    @Autowired
    private IProductoService productoService;

    @Autowired
    private ICategoriaService categoriaService;

    @Autowired
    private IImagenProductoService imagenService;

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
    public String guardar(@Valid Producto producto, 
                        BindingResult result,
                        @RequestParam("imagenes") MultipartFile[] archivos,
                        Model model, 
                        RedirectAttributes flash) {

        // Validación del formulario
        if (result.hasErrors()) {
            model.addAttribute("categorias", categoriaService.buscarTodo());
            model.addAttribute("error", "Por favor corrija los errores en el formulario");
            return "productos/form";
        }

        try {
            logger.info("Intentando guardar producto: {}", producto.getNombre());
            
            // 1. Guardar el producto primero para obtener ID
            producto = productoService.guardar(producto);
            logger.info("Producto guardado con ID: {}", producto.getId());

            // 2. Procesar imágenes si existen
            if (archivos != null && archivos.length > 0 && !archivos[0].isEmpty()) {
                logger.info("Procesando {} imágenes", archivos.length);
                
                for (MultipartFile archivo : archivos) {
                    if (!archivo.isEmpty()) {
                        try {
                            ImagenProducto imagen = imagenService.guardarImagen(archivo);
                            imagen.setProducto(producto);
                            producto.agregarImagen(imagen);
                            logger.info("Imagen guardada: {}", imagen.getNombreArchivo());
                        } catch (IOException e) {
                            logger.error("Error al guardar imagen: {}", e.getMessage());
                            flash.addFlashAttribute("error", "Error al guardar imagen: " + e.getMessage());
                        }
                    }
                }
                // Actualizar producto con las imágenes
                productoService.guardar(producto);
            }

            // Mensaje de éxito
            String mensaje = (producto.getId() == null) ? 
                "Producto creado exitosamente" : "Producto actualizado exitosamente";
            String alertClass = (producto.getId() == null) ? "success" : "warning";
            
            flash.addFlashAttribute("info", mensaje);
            flash.addFlashAttribute("alertClass", alertClass);
            
            return "redirect:/productos/listado";

        } catch (Exception e) {
            logger.error("Error al guardar producto", e);
            model.addAttribute("categorias", categoriaService.buscarTodo());
            model.addAttribute("error", "Error crítico: " + e.getMessage());
            return "productos/form";
        }
    }
    
    @GetMapping("/detalle/{id}")
    public String verDetalle(@PathVariable("id") Long id, Model model) {
        Producto producto = productoService.buscarPorId(id);
        List<Producto> productosRelacionados = productoService.buscarPorCategoria(producto.getCategoria().getId());
        
        model.addAttribute("producto", producto);
        model.addAttribute("productosRelacionados", productosRelacionados);
        model.addAttribute("titulo", "Detalle: " + producto.getNombre());

        return "productos/detalles";
    }

    @PostMapping("/categorias/guardar")
    public String guardarCategoria(@Valid Categoria categoria, 
                                BindingResult result, 
                                RedirectAttributes flash, 
                                HttpServletRequest request) {
        if (result.hasErrors()) {
            flash.addFlashAttribute("error", "Corrija los errores en la categoría");
            return "redirect:" + request.getHeader("Referer");
        }
    
        categoriaService.guardar(categoria);
        flash.addFlashAttribute("info", "Categoría guardada: " + categoria.getNombre());
        return "redirect:" + request.getHeader("Referer");
    }

    @GetMapping("/eliminar-imagen/{productoId}/{imagenId}")
    public String eliminarImagen(@PathVariable Long productoId, 
                              @PathVariable Long imagenId,
                              RedirectAttributes flash) {
        try {
            imagenService.eliminarPorId(imagenId);
            flash.addFlashAttribute("info", "Imagen eliminada correctamente");
        } catch (Exception e) {
            logger.error("Error al eliminar imagen", e);
            flash.addFlashAttribute("error", "No se pudo eliminar la imagen");
        }
        return "redirect:/productos/editar/" + productoId;
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
        try {
            productoService.borrarPorId(id);
            flash.addFlashAttribute("info", "Producto eliminado correctamente");
            flash.addFlashAttribute("alertClass", "success");
        } catch (Exception e) {
            logger.error("Error al eliminar producto", e);
            flash.addFlashAttribute("error", "No se pudo eliminar el producto");
            flash.addFlashAttribute("alertClass", "danger");
        }
        return "redirect:/productos/listado";
    }
}