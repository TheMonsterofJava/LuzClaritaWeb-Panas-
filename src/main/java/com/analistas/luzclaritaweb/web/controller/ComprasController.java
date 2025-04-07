package com.analistas.luzclaritaweb.web.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.analistas.luzclaritaweb.model.domain.Compra;
import com.analistas.luzclaritaweb.model.domain.DetalleCompra;
import com.analistas.luzclaritaweb.model.domain.Inventario;
import com.analistas.luzclaritaweb.model.domain.Usuario;
import com.analistas.luzclaritaweb.model.service.ICajaService;
import com.analistas.luzclaritaweb.model.service.ICompraService;
import com.analistas.luzclaritaweb.model.service.IInventarioService;
import com.analistas.luzclaritaweb.model.service.IProveedorService;
import com.analistas.luzclaritaweb.model.service.IUsuariosService;

@Controller
@RequestMapping("/compras")
public class ComprasController {

    @Autowired
    private ICompraService compraService;

    @Autowired
    private IProveedorService proveedorService;

    @Autowired
    private ICajaService cajaService;

    @Autowired
    private IUsuariosService usuarioService;

    // @Autowired
    // private IProductoService productoService;

    @Autowired
    private IInventarioService inventarioService;

    @GetMapping("/listado")
    public String listar(Model model) {
        model.addAttribute("titulo", "Listado de Compras");
        model.addAttribute("compras", compraService.listarCompras());
        return "compras/listado";
    }

    @GetMapping("/nueva")
    public String nuevaCompra(Model model, Authentication authentication) {
        Compra compra = new Compra();
        compra.setDetalles(new ArrayList<>());

        // Obtener usuario autenticado
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Usuario usuario = usuarioService.buscarPorNombreUsuario(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        compra.setUsuario(usuario);

        model.addAttribute("compra", compra);
        model.addAttribute("proveedores", proveedorService.buscarTodo());
        model.addAttribute("cajasAbiertas", cajaService.listarCajasAbiertas());
        model.addAttribute("ingredientes", inventarioService.buscarTodo()); // <--- Lista de ingredientes
        return "compras/form";
    }

     @PostMapping("/guardar")
    public String guardarCompra(
            @ModelAttribute("compra") Compra compra,
            RedirectAttributes redirect,
            Authentication auth) {

        // 1. Validar y asignar detalles
        for (DetalleCompra detalle : compra.getDetalles()) {
            detalle.setCompra(compra);

            // 2. Actualizar stock del ingrediente
            Inventario ingrediente = inventarioService.buscarPorId(detalle.getIngrediente().getId());
            ingrediente.setCantidad(ingrediente.getCantidad() + detalle.getCantidad()); // Usar int
            inventarioService.guardar(ingrediente);
        }

        // 3. Calcular total (opcional)
        BigDecimal total = compra.getDetalles().stream()
                .map(d -> d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        compra.setTotal(total);

        compraService.guardarCompra(compra);
        redirect.addFlashAttribute("success", "Compra registrada!");
        return "redirect:/compras/listado";
    }



    @GetMapping("/editar/{id}")
    public String editarCompra(@PathVariable("id") Long id, Model model) {
        Compra compra = compraService.buscarPorIdConDetalles(id);
        model.addAttribute("compra", compra);
        model.addAttribute("proveedores", proveedorService.buscarTodo());
        model.addAttribute("cajasAbiertas", cajaService.listarCajasAbiertas());
        return "compras/form";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarCompra(@PathVariable("id") Long id, RedirectAttributes redirect) {
        compraService.eliminarCompra(id);
        redirect.addFlashAttribute("warning", "Compra eliminada correctamente");
        return "redirect:/compras/listado";
    }

    @GetMapping("/buscar")
    public String buscarPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin,
            Model model) {

        model.addAttribute("compras", compraService.buscarPorRangoDeFechas(inicio, fin));
        return "compras/listado";
    }
}


    // @GetMapping("/nueva")
    // public String nuevaCompra(Model model) {
    // Compra compra = new Compra();
    // compra.setDetalles(new ArrayList<>()); // Inicializar lista
    // model.addAttribute("compra", compra);
    // model.addAttribute("proveedores", proveedorService.buscarTodo());
    // model.addAttribute("cajasAbiertas", cajaService.listarCajasAbiertas());

    // return "compras/form";
    // }


  // @PostMapping("/guardar")
    // public String guardarCompra(
    // @ModelAttribute Compra compra,
    // RedirectAttributes redirect,
    // Authentication authentication) { // Obtiene la autenticación actual

    // // Obtener el usuario autenticado
    // UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    // Usuario usuario =
    // usuarioService.buscarPorNombreUsuario(userDetails.getUsername())
    // .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " +
    // userDetails.getUsername()));

    // // Asignar el usuario a la compra
    // compra.setUsuario(usuario);

    // // Calcular el total si es necesario
    // if (compra.getTotal() == null) {
    // compra.setTotal(compra.getDetalles().stream()
    // .map(d -> d.getPrecioActual().multiply(BigDecimal.valueOf(d.getCantidad())))
    // .reduce(BigDecimal.ZERO, BigDecimal::add));
    // }

    // compraService.guardarCompra(compra);

    // redirect.addFlashAttribute("success", "Compra registrada correctamente");
    // return "redirect:/compras/listado";
    // }

        // @PostMapping("/guardar")
    // public String guardarCompra(
    //         @ModelAttribute("compra") Compra compra,
    //         RedirectAttributes redirect,
    //         Authentication authentication) {

    //     // Obtener usuario autenticado
    //     UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    //     Usuario usuario = usuarioService.buscarPorNombreUsuario(userDetails.getUsername())
    //             .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    //     compra.setUsuario(usuario);

    //     // Asignar la compra a cada detalle y validar productos
    //     for (DetalleCompra detalle : compra.getDetalles()) {
    //         detalle.setCompra(compra);

    //         // Si el producto es nuevo (sin ID), buscarlo o crearlo
    //         if (detalle.getProducto() != null && detalle.getProducto().getId() == null) {
    //             // Buscar producto existente por nombre o crear uno nuevo
    //             Producto productoExistente = productoService.buscarPorNombre(detalle.getProducto().getDescripcion()); //En la descripcion se almacena el nombre del producto
    //             detalle.setProducto(productoExistente != null ? productoExistente : productoService.guardar(detalle.getProducto()));
    //         }
    //     }

    //     // Calcular el total
    //     compra.setTotal(compra.getDetalles().stream()
    //             .map(d -> d.getPrecioActual().multiply(BigDecimal.valueOf(d.getCantidad())))
    //             .reduce(BigDecimal.ZERO, BigDecimal::add));

    //     compraService.guardarCompra(compra);

    //     redirect.addFlashAttribute("success", "Compra registrada correctamente");
    //     return "redirect:/compras/listado";
    // }