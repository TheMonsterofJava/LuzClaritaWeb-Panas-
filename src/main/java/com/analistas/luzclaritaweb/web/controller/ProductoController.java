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
import com.analistas.luzclaritaweb.model.service.interfaces.ICategoriaService;
import com.analistas.luzclaritaweb.model.service.interfaces.IProductoService;
import com.analistas.luzclaritaweb.model.service.interfaces.IUploadFileService;

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

    @Autowired
    private IUploadFileService uploadFileService;

    @PostMapping("/guardar")
    public String guardar(@Valid Producto producto, BindingResult result,
        Model model, SessionStatus status, RedirectAttributes flash,
        @org.springframework.web.bind.annotation.RequestParam("imagen") MultipartFile imagen) {

        if (result.hasErrors()) {
            model.addAttribute("titulo", "Corrija los errores");
            model.addAttribute("error", "Corrija los errores del formulario");
            model.addAttribute("categorias", categoriaService.buscarTodo());
            return "productos/form";
        }

        // Si se subió un archivo de imagen
        if (!imagen.isEmpty()) {
            // Si el producto ya tiene una imagen y se está subiendo una nueva, borrar la anterior
            if (producto.getId() != null && producto.getId() > 0 && producto.getLinkImagen() != null && producto.getLinkImagen().length() > 0) {
                uploadFileService.delete(producto.getLinkImagen());
            }
            
            String uniqueFilename = null;
            try {
                // Copiar la nueva imagen al directorio de subidas
                uniqueFilename = uploadFileService.copy(imagen);
                flash.addFlashAttribute("info", "Imagen subida correctamente: " + uniqueFilename);
                // Guardar la ruta de la imagen en el producto
                producto.setLinkImagen(uniqueFilename);
            } catch (Exception e) {
                model.addAttribute("error", "Error al subir la imagen: " + e.getMessage());
                model.addAttribute("categorias", categoriaService.buscarTodo());
                return "productos/form";
            }
        } 
        // Si no se sube una nueva imagen, el campo producto.linkImagen (poblado por el input de texto) se mantiene.

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
        Producto producto = productoService.buscarPorId(id);
        // Eliminar imagen física si existe
        if (producto != null && producto.getLinkImagen() != null && !producto.getLinkImagen().isEmpty()) {
            try {
                // Elimina el primer '/' si existe
                String rutaRelativa = producto.getLinkImagen().startsWith("/") ? producto.getLinkImagen().substring(1) : producto.getLinkImagen();
                Path rutaImagen = Paths.get(rutaRelativa).toAbsolutePath();
                Files.deleteIfExists(rutaImagen);
            } catch (Exception e) {
                flash.addFlashAttribute("error", "No se pudo eliminar la imagen asociada al producto.");
            }
        }
        productoService.borrarPorId(id);
        flash.addFlashAttribute("info", "Producto eliminado correctamente");
        flash.addFlashAttribute("alertClass", "alert-danger");
        return "redirect:/productos/listado2";
    }
}