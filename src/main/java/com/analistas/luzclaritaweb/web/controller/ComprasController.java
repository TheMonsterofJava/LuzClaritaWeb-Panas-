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
import com.analistas.luzclaritaweb.model.repository.ICompraRepository;
import com.analistas.luzclaritaweb.model.repository.IMovimientoCajaRepository;
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

    @Autowired
    private ICompraRepository compraRepository;

    @Autowired
    private IMovimientoCajaRepository movimientoCajaRepository;

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

        // Setear usuario autenticado
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        Usuario usuario = usuarioService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        compra.setUsuario(usuario);

        // Validar que la compra tenga detalles
        if (compra.getDetalles() == null || compra.getDetalles().isEmpty()) {
            redirect.addFlashAttribute("error", "No puedes guardar una compra sin detalles.");
            return "redirect:/compras/nueva";
        }

        // Calcular nuevo total
        BigDecimal nuevoTotal = compra.getDetalles().stream()
                .map(d -> d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        compra.setTotal(nuevoTotal);

        // Lógica para EDITAR una compra existente
        if (compra.getId() != null) {
            // 1. Obtener estado anterior de la compra
            Compra compraAnterior = compraRepository.findById(compra.getId())
                    .orElseThrow(() -> new RuntimeException("Compra no encontrada"));
            BigDecimal totalAnterior = compraAnterior.getTotal();

            // 2. Revertir stock anterior
            for (DetalleCompra detalleAnterior : compraAnterior.getDetalles()) {
                Inventario ingrediente = detalleAnterior.getIngrediente();
                ingrediente.setCantidad(ingrediente.getCantidad() - detalleAnterior.getCantidad());
                inventarioService.guardar(ingrediente);
            }

            // 3. Actualizar stock con lo nuevo
            for (DetalleCompra detalleNuevo : compra.getDetalles()) {
                Inventario ingrediente = inventarioService.buscarPorId(detalleNuevo.getIngrediente().getId());
                ingrediente.setCantidad(ingrediente.getCantidad() + detalleNuevo.getCantidad());
                inventarioService.guardar(ingrediente);
                detalleNuevo.setCompra(compra);
            }

            // 4. Guardar la compra actualizada
            compra.setFechaHora(compraAnterior.getFechaHora()); // Mantener fecha original
            compraService.guardarCompra(compra); // Sin asignar a variable

            // 5. Actualizar movimiento de caja
            MovimientoCaja movimiento = movimientoCajaRepository.findByCompraId(compra.getId());
            if (movimiento != null) {
                movimiento.setMonto(nuevoTotal);
                movimiento.setFecha(LocalDateTime.now()); // Actualizar fecha del movimiento
                // Añadir prefijo "MODIFICADO" si no existe ya un prefijo de estado
                if (!movimiento.getDescripcion().startsWith("MODIFICADO - ")
                        && !movimiento.getDescripcion().startsWith("ANULADO - ")) {
                    movimiento.setDescripcion("MODIFICADO - " + movimiento.getDescripcion());
                }
                movimientoCajaService.guardarMovimiento(movimiento);

                // 6. Actualizar saldo de la caja
                Caja caja = cajaService.obtenerCajaPorId(compra.getCaja().getId())
                        .orElseThrow(() -> new RuntimeException("Caja no encontrada"));
                // Ajustar saldo: sumar lo de antes, restar lo nuevo
                BigDecimal saldoAjustado = caja.getSaldoFinal().add(totalAnterior).subtract(nuevoTotal);
                caja.setSaldoFinal(saldoAjustado);
                cajaService.guardarCaja(caja);
            }

            redirect.addFlashAttribute("success", "Compra modificada correctamente!");

        } else { // Lógica para CREAR una nueva compra
            // 1. Asignar detalles y actualizar stock
            for (DetalleCompra detalle : compra.getDetalles()) {
                detalle.setCompra(compra);
                Inventario ingrediente = inventarioService.buscarPorId(detalle.getIngrediente().getId());
                ingrediente.setCantidad(ingrediente.getCantidad() + detalle.getCantidad());
                inventarioService.guardar(ingrediente);
            }

            // 2. Guardar la compra
            Compra compraGuardada = compraService.guardarCompra(compra);

            // 3. Crear movimiento de caja
            MovimientoCaja egreso = new MovimientoCaja();
            egreso.setFecha(LocalDateTime.now());
            egreso.setMonto(nuevoTotal);
            egreso.setTipo("EGRESO");
            egreso.setTipoOperacion(MovimientoCaja.TipoOperacion.EGRESO);
            Proveedor proveedor = proveedorService.buscarPorId(compra.getProveedor().getId());
            egreso.setDescripcion("Compra a Proveedor: " + (proveedor != null ? proveedor.getNombre() : "Desconocido"));
            egreso.setCaja(compraGuardada.getCaja());
            egreso.setOperador(usuario);
            egreso.setCompra(compraGuardada);
            movimientoCajaService.guardarMovimiento(egreso);

            // 4. Actualizar saldo de la caja
            Caja caja = cajaService.obtenerCajaPorId(compraGuardada.getCaja().getId())
                    .orElseThrow(() -> new RuntimeException("Caja no encontrada"));
            caja.setSaldoFinal(caja.getSaldoFinal().subtract(nuevoTotal));
            cajaService.guardarCaja(caja);

            redirect.addFlashAttribute("success", "Compra registrada!");
        }

        return "redirect:/compras/listado";
    }

    @GetMapping("/editar/{id}")
    public String editarCompra(@PathVariable("id") Long id, Model model) {
        Compra compra = compraService.buscarPorIdConDetalles(id);
        model.addAttribute("compra", compra);
        model.addAttribute("proveedores", proveedorService.buscarTodo());
        model.addAttribute("cajasAbiertas", cajaService.listarCajasAbiertas());
        model.addAttribute("ingredientes", inventarioService.buscarTodo()); // <-- Añadido

        return "compras/form";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarCompra(@PathVariable("id") Long id, RedirectAttributes redirect) {
        try {
            // Buscar la compra y sus detalles para que todo este cargado:
            Compra compra = compraService.buscarPorIdConDetalles(id);
            if (compra == null) {
                redirect.addFlashAttribute("error", "La compra no existe.");
                return "redirect:/compras/listado";
            }

            // 2. Revertir el stock de los ingredientes antes de eliminar
            for (DetalleCompra detalle : compra.getDetalles()) {
                Inventario ingrediente = detalle.getIngrediente();
                if (ingrediente != null) {
                    ingrediente.setCantidad(ingrediente.getCantidad() - detalle.getCantidad());
                    inventarioService.guardar(ingrediente);
                }
            }

            // 3. Anular el movimiento de caja y revertir el saldo
            MovimientoCaja movimiento = movimientoCajaRepository.findByCompraId(id);
            if (movimiento != null) {
                // Revertir saldo
                Caja caja = movimiento.getCaja();
                if (caja != null && movimiento.getTipoOperacion() == MovimientoCaja.TipoOperacion.EGRESO) {
                    caja.setSaldoFinal(caja.getSaldoFinal().add(movimiento.getMonto()));
                    cajaService.guardarCaja(caja);
                }

                // Anular el movimiento en lugar de eliminarlo
                movimiento.setEstado(MovimientoCaja.EstadoMovimiento.ANULADO);
                movimiento.setDescripcion("ANULADO - " + movimiento.getDescripcion());
                movimiento.setCompra(null); // Desvincular de la compra que se va a eliminar
                movimientoCajaService.guardarMovimiento(movimiento);
            }

            // 4. Eliminar la compra (los detalles se van en cascada)
            compraService.eliminarCompra(id);

            redirect.addFlashAttribute("success", "Compra eliminada permanentemente y stock revertido.");

        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Error al eliminar la compra: " + e.getMessage());
        }

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