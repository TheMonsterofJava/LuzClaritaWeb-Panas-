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

import com.analistas.luzclaritaweb.model.domain.Caja;
import com.analistas.luzclaritaweb.model.domain.Compra;
import com.analistas.luzclaritaweb.model.domain.DetalleCompra;
import com.analistas.luzclaritaweb.model.domain.Inventario;
import com.analistas.luzclaritaweb.model.domain.MovimientoCaja;
import com.analistas.luzclaritaweb.model.domain.Proveedor;
import com.analistas.luzclaritaweb.model.domain.Usuario;
import com.analistas.luzclaritaweb.model.service.interfaces.ICajaService;
import com.analistas.luzclaritaweb.model.service.interfaces.ICompraService;
import com.analistas.luzclaritaweb.model.service.interfaces.IInventarioService;
import com.analistas.luzclaritaweb.model.service.interfaces.IMovimientoCajaService;
import com.analistas.luzclaritaweb.model.service.interfaces.IProveedorService;
import com.analistas.luzclaritaweb.model.service.interfaces.IUsuariosService;

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

    @Autowired
    private IMovimientoCajaService movimientoCajaService;

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
        // Esta linea de codigo funciona para obtener el usuario autenticado 
        Usuario usuario = usuarioService.findByEmail(userDetails.getUsername())

                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        compra.setUsuario(usuario);

        model.addAttribute("compra", compra);
        model.addAttribute("proveedores", proveedorService.buscarTodo());
        model.addAttribute("cajasAbiertas", cajaService.listarCajasAbiertas());
        model.addAttribute("ingredientes", inventarioService.buscarTodo()); // <--- Lista de ingredientes
        return "compras/form";
    }

    // Se agrega el método guardarCompra para manejar la lógica de compra
    // y actualizar el inventario de ingredientes.
    // La logica incluye:
    // 1. Validar y asignar detalles de compra.
    // 2. Actualizar stock del ingrediente.
    // 3. Calcular total de la compra.
    // Esta misma se encarga de registrar la compra a los proveedores,
    // registrar el movimiento de caja tipo EGRESO, y actualizar la caja.
    @PostMapping("/guardar")
    public String guardarCompra(
            @ModelAttribute("compra") Compra compra,
            RedirectAttributes redirect,
            Authentication auth) {

        // Setear usuario autenticado a la compra a proveedores:
        if (auth != null && auth.getPrincipal() instanceof UserDetails userDetails) {
            // Esta linea de codigo funciona para obtener el usuario autenticado 
            Usuario usuario = usuarioService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            compra.setUsuario(usuario);
        } else {
            redirect.addFlashAttribute("error", "No se pudo obtener el usuario autenticado.");
            return "redirect:/compras/nueva";
        }

        // 1. Validar y asignar detalles
        for (DetalleCompra detalle : compra.getDetalles()) {
            detalle.setCompra(compra);

            // Solucion al error de producto nulo:
            detalle.setProducto(null); // Aseguramos que el producto sea nulo, ya que estamos trabajando con
                                       // ingredientes

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

        // Validar que la compra tenga detalles
        // Si no tiene detalles, redirigir con mensaje de error
        if (compra.getDetalles() == null || compra.getDetalles().isEmpty()) {
            redirect.addFlashAttribute("error", "No puedes guardar una compra sin detalles.");
            return "redirect:/compras/nueva";
        }

        // Guardar la compra y obtener la instancia persistida (con ID)
        Compra compraGuardada = compraService.guardarCompra(compra);

        // 4. Registramos el movimiento de caja tipo (EGRESO),
        MovimientoCaja egreso = new MovimientoCaja();
        egreso.setFecha(LocalDateTime.now());
        egreso.setMonto(total); // total ya es BigDecimal
        // egreso.setMontoDouble(total.doubleValue()); // Eliminado
        egreso.setTipo("EGRESO");
        egreso.setTipoOperacion(MovimientoCaja.TipoOperacion.EGRESO);
        // Obtener el nombre del proveedor de forma segura
        String nombreProveedor = "Desconocido";
        if (compra.getProveedor() != null && compra.getProveedor().getId() != null) {
            Proveedor proveedorRecargado = proveedorService.buscarPorId(compra.getProveedor().getId());
            if (proveedorRecargado != null) {
                if (proveedorRecargado.getNombre() != null && !proveedorRecargado.getNombre().isEmpty()) {
                    nombreProveedor = proveedorRecargado.getNombre();
                }
            }
        }
        egreso.setDescripcion("Compra a Proveedor: " + nombreProveedor);

        // Asignar la caja de la compra al movimiento de egreso
        // Es importante que compra.getCaja() no sea nulo aquí.
        // Se asume que se valida o se asigna una caja a la compra antes de este punto.
        if (compraGuardada.getCaja() == null || compraGuardada.getCaja().getId() == null) {
            redirect.addFlashAttribute("error", "La compra guardada no tiene una caja asignada.");
            // Esto podría indicar un problema si la caja es obligatoria para la compra.
            // Considerar si se debe deshacer la compra o manejar de otra forma.
            return "redirect:/compras/nueva";
        }
        egreso.setCaja(compraGuardada.getCaja());
        egreso.setOperador(compraGuardada.getUsuario());
        egreso.setCompra(compraGuardada); // Asociar la compra al movimiento de caja
        movimientoCajaService.guardarMovimiento(egreso);

        // 5. Actualizar caja
        // Recargar la entidad Caja para asegurar que operamos sobre el estado más
        // reciente
        Caja cajaParaActualizar = cajaService.obtenerCajaPorId(compraGuardada.getCaja().getId())
                .orElseThrow(() -> new RuntimeException(
                        "Caja con ID " + compraGuardada.getCaja().getId() + " no encontrada para actualizar."));

        // Asegurar que el saldo final no sea nulo antes de operar
        BigDecimal saldoFinalActual = cajaParaActualizar.getSaldoFinal() != null ? cajaParaActualizar.getSaldoFinal()
                : BigDecimal.ZERO;

        BigDecimal nuevoSaldoFinal = saldoFinalActual.subtract(total);
        cajaParaActualizar.setSaldoFinal(nuevoSaldoFinal);
        // El estado de la caja debería manejarse consistentemente. Si una compra
        // implica que la caja está abierta,
        // este estado ya debería estar así o ser validado. Forzarlo aquí podría no ser
        // siempre correcto.
        // cajaParaActualizar.setEstado(Caja.EstadoCaja.ABIERTA);
        cajaService.guardarCaja(cajaParaActualizar);

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
// @ModelAttribute("compra") Compra compra,
// RedirectAttributes redirect,
// Authentication authentication) {

// // Obtener usuario autenticado
// UserDetails userDetails = (UserDetails) authentication.getPrincipal();
// Usuario usuario =
// usuarioService.buscarPorNombreUsuario(userDetails.getUsername())
// .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
// compra.setUsuario(usuario);

// // Asignar la compra a cada detalle y validar productos
// for (DetalleCompra detalle : compra.getDetalles()) {
// detalle.setCompra(compra);

// // Si el producto es nuevo (sin ID), buscarlo o crearlo
// if (detalle.getProducto() != null && detalle.getProducto().getId() == null) {
// // Buscar producto existente por nombre o crear uno nuevo
// Producto productoExistente =
// productoService.buscarPorNombre(detalle.getProducto().getDescripcion()); //En
// la descripcion se almacena el nombre del producto
// detalle.setProducto(productoExistente != null ? productoExistente :
// productoService.guardar(detalle.getProducto()));
// }
// }

// // Calcular el total
// compra.setTotal(compra.getDetalles().stream()
// .map(d -> d.getPrecioActual().multiply(BigDecimal.valueOf(d.getCantidad())))
// .reduce(BigDecimal.ZERO, BigDecimal::add));

// compraService.guardarCompra(compra);

// redirect.addFlashAttribute("success", "Compra registrada correctamente");
// return "redirect:/compras/listado";
// }