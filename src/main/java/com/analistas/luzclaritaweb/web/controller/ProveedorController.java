package com.analistas.luzclaritaweb.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.analistas.luzclaritaweb.model.domain.Proveedor;
import com.analistas.luzclaritaweb.model.service.IProveedorService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/proveedor")
@SessionAttributes("proveedor")
public class ProveedorController {

    @Autowired
    private IProveedorService proveedorService;

    @GetMapping("/listado")
    public String listar(Model model) {

        model.addAttribute("titulo", "Listado de Proveedores");
        model.addAttribute("proveedorList", proveedorService.buscarTodo());
        return "proveedor/list";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {

        model.addAttribute("titulo", "Nuevo Proveedor");
        model.addAttribute("proveedor", new Proveedor()); // Objeto vacío para el formulario
        return "proveedor/form";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {

        Proveedor proveedor = proveedorService.buscarPorId(id);
        model.addAttribute("titulo", "Editar Proveedor");
        model.addAttribute("proveedor", proveedor);
        return "proveedor/form";

    }

    @PostMapping("/guardar")
    public String guardar(@Valid Proveedor proveedor,
            BindingResult result,
            Model model,
            SessionStatus status,
            RedirectAttributes flash) {

        // Verificar si hay errores...
        if (result.hasErrors()) {
            model.addAttribute("titulo", proveedor.getId() != null ? "Editar Proveedor" : "Nuevo Proveedor");
            model.addAttribute("error", "Por favor corrija los errores");
            return "proveedor/form";
        }

        String mensaje = proveedor.getId() != null ? "Proveedor actualizado correctamente"
                : "Proveedor creado correctamente";

        proveedorService.guardar(proveedor);
        status.setComplete();
        flash.addFlashAttribute("success", mensaje);

        return "redirect:/proveedor/listado";
    }

    // @GetMapping("/cambiar-estado/{id}")
    // public String cambiarEstado(@PathVariable Long id, RedirectAttributes flash)
    // {
    // Proveedor proveedor = proveedorService.buscarPorId(id);
    // proveedor.setActivo(!proveedor.isActivo());
    // proveedorService.guardar(proveedor);

    // String mensaje = proveedor.isActivo() ?
    // "Proveedor " + proveedor.getNombre() + " habilitado" :
    // "Proveedor " + proveedor.getNombre() + " deshabilitado";

    // flash.addFlashAttribute("info", mensaje);
    // return "redirect:/proveedor/listado";
    // }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        // Busca el proveedor en la base de datos
        Proveedor proveedor = proveedorService.buscarPorId(id);

        if (proveedor != null) {
            proveedorService.borrarPorId(id); // Llama al servicio para eliminar el registro
            flash.addFlashAttribute("success", "Proveedor " + proveedor.getNombre() + " eliminado correctamente.");
        } else {
            flash.addFlashAttribute("error", "El proveedor no existe en la base de datos.");
        }

        return "redirect:/proveedor/listado";
    }

}

// @Controller
// @RequestMapping("/proveedor")
// @SessionAttributes("proveedor")
// public class ProveedorController {

// @Autowired
// IProveedorService proveedorService;

// @Autowired
// private ICompraService compraService;

// @Autowired
// private IMovimientoCajaService movimientoCajaService;

// @Autowired
// private IProductoService productoService;

// // Método para listar los productos
// @GetMapping("/listado")
// public String listar(Model model) {

// model.addAttribute("titulo", "Listado de Proveedores");
// model.addAttribute("proveedorList", proveedorService.buscarTodo());

// return "proveedor/list"; // Asegúrate de que esta vista sea la correcta
// }

// // Método para mostrar el formulario y guardar un nuevo producto

// @GetMapping("/nuevo")
// public String nuevo(Model model) {

// model.addAttribute("titulo", "Nuevo Proveedor");
// model.addAttribute("proveedor", new Proveedor()); // Objeto vacío para el
// formulario

// return "proveedor/form"; // Vista del formulario
// }

// @GetMapping("/editar/{id}")
// public String editar(@PathVariable("id") Long id, Model model) {

// Proveedor proveedor = proveedorService.buscarPorId(id);

// model.addAttribute("titulo", "Editar Proveedor");
// model.addAttribute("proveedor", proveedor);

// return "proveedor/form";
// }

// @PostMapping("/guardar")
// public String guardar(@Valid Proveedor proveedor, BindingResult result,
// Model model, SessionStatus status) {

// // Verificar si hay errores...
// if (result.hasErrors()) {
// model.addAttribute("error", "corrija los errores");
// return "proveedor/form";

// }

// proveedorService.guardar(proveedor);
// status.setComplete();

// return "redirect:/proveedor/listado";
// }

// @GetMapping("/borrar/{id}")
// public String cambiarEstado(@PathVariable Long id, RedirectAttributes flash)
// {

// Proveedor proveedor = proveedorService.buscarPorId(id);
// proveedor.setActivo(!proveedor.isActivo()); // Si está activo, lo desactivo y
// viceversa
// proveedorService.guardar(proveedor);

// String mensaje = proveedor.isActivo() ? "registro de " +
// proveedor.getNombre() + " habilitado"
// : "registro de " + proveedor.getNombre() + " Deshabilitado";
// flash.addFlashAttribute("info", mensaje);

// return "redirect:/proveedor/listado";
// }

// @ModelAttribute("compras")
// public List<Compra> listarCompras() {
// return proveedorService.getCompras();
// }

// @PostMapping("/compras/guardar")
// public String guardarCompra(@Valid Compra compra,
// BindingResult result,
// @AuthenticationPrincipal Usuario usuario,
// RedirectAttributes flash) {

// if (result.hasErrors()) {
// flash.addFlashAttribute("error", "Corrija los errores en el formulario");
// return "redirect:/proveedor/compras/nuevo";
// }

// compra.setFechaHora(LocalDateTime.now());
// compraService.guardar(compra); // Usar la instancia inyectada

// // Registrar movimiento de caja
// movimientoCajaService.registrarMovimiento(compra, usuario); // Usar la
// instancia inyectada

// // Actualizar inventario - usar el nombre correcto del campo
// actualizarInventarioCompra(compra.getCompras()); // Cambiado de getDetalles()
// a getCompras()

// flash.addFlashAttribute("success", "Compra registrada correctamente");
// return "redirect:/proveedor/compras/listado";
// }

// private void actualizarInventarioCompra(List<DetalleCompra> detalles) {
// for (DetalleCompra detalle : detalles) {
// if (detalle.getProducto() != null) {
// Producto producto = detalle.getProducto();
// producto.setStock(producto.getStock() + detalle.getCantidad());
// // Aquí deberías guardar el producto actualizado
// productoService.guardar(producto); // Necesitarás inyectar IProductoService
// }
// }
// }

// }
