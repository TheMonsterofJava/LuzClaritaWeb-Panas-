package com.analistas.luzclaritaweb.web.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.analistas.luzclaritaweb.model.domain.Caja;
import com.analistas.luzclaritaweb.model.domain.MovimientoCaja;
import com.analistas.luzclaritaweb.model.domain.Usuario;
import com.analistas.luzclaritaweb.model.service.ICajaService;
import com.analistas.luzclaritaweb.model.service.IMovimientoCajaService;
import com.analistas.luzclaritaweb.model.service.IUsuariosService;

@Controller
@RequestMapping("/caja")
public class CajaController {

    private final PasswordEncoder passwordEncoder;

    @Autowired
    private ICajaService cajaService;

    @Autowired
    private IMovimientoCajaService movimientoCajaService;

    @Autowired
    private IUsuariosService usuarioService;

    //Constructor para inyectar PasswordEncoder
    public CajaController(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    // Listar todas las cajas
    @GetMapping("/listado")
    public String listadoCajas(Model model) {
        List<Caja> cajas = cajaService.listarCajas();   
        model.addAttribute("titulo", "Administración de Cajas");
        model.addAttribute("cajas", cajas);
        return "movimientos/caja";
    }

    // Ver detalle de una caja
    @GetMapping("/detalle/{id}")
    public String detalleCaja(@PathVariable("id") Long id, Model model) {
        Optional<Caja> cajaOpt = cajaService.obtenerCajaPorId(id);
        if (cajaOpt.isEmpty()) {
            model.addAttribute("error", "Caja no encontrada.");
            return "redirect:/caja/listado";
        }
        Caja caja = cajaOpt.get();
        model.addAttribute("caja", caja);
        model.addAttribute("movimientos", movimientoCajaService.buscarPorCaja(id));
        return "movimientos/detalle_caja"; // crea esta vista si quieres detalle extendido
    }

    // Abrir una caja
    @GetMapping("/nueva")
    public String nuevaCajaForm(Model model) {
        model.addAttribute("caja", new Caja());
        return "movimientos/caja_form";
    }

    @PostMapping("/guardar")
    public String guardarCaja(@ModelAttribute Caja caja, RedirectAttributes flash) {
        caja.setEstado(Caja.EstadoCaja.ABIERTA);
        caja.setSaldoFinal(caja.getSaldoInicial());
        cajaService.guardarCaja(caja);
        flash.addFlashAttribute("success", "Caja abierta correctamente.");
        return "redirect:/caja/listado";
    }

    // Cerrar una caja
    @GetMapping("/cerrar/{id}")
    public String cerrarCaja(@PathVariable("id") Long id, RedirectAttributes flash) {
        Optional<Caja> cajaOpt = cajaService.obtenerCajaPorId(id);
        if (cajaOpt.isPresent()) {
            Caja caja = cajaOpt.get();
            caja.setEstado(Caja.EstadoCaja.CERRADA);
            cajaService.guardarCaja(caja);
            flash.addFlashAttribute("success", "Caja cerrada correctamente.");
        } else {
            flash.addFlashAttribute("error", "No se encontró la caja.");
        }
        return "redirect:/caja/listado";
    }

    // Abrir una caja cerrada (re-abrir)
    @GetMapping("/abrir/{id}")
    public String abrirCaja(@PathVariable("id") Long id, RedirectAttributes flash) {
        Optional<Caja> cajaOpt = cajaService.obtenerCajaPorId(id);
        if (cajaOpt.isPresent()) {
            Caja caja = cajaOpt.get();
            caja.setEstado(Caja.EstadoCaja.ABIERTA);
            cajaService.guardarCaja(caja);
            flash.addFlashAttribute("success", "Caja abierta nuevamente.");
        } else {
            flash.addFlashAttribute("error", "No se encontró la caja.");
        }
        return "redirect:/caja/listado";
    }

    // Historial de movimientos de una caja específica
    @GetMapping("/movimientos")
    public String movimientosPorCaja(
            @RequestParam(value = "cajaId", required = false) Long cajaId,
            @RequestParam(name = "fechaInicio", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaInicio,
            @RequestParam(name = "fechaFin", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaFin,
            Model model) {

        if (cajaId == null) {
            model.addAttribute("error", "Debe seleccionar una caja.");
            return "redirect:/caja/listado";
        }

        LocalDateTime inicio = (fechaInicio != null) ? fechaInicio.atStartOfDay()
                : LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime fin = (fechaFin != null) ? fechaFin.atTime(23, 59, 59) : LocalDateTime.now();

        // Validar que la caja existe
        List<MovimientoCaja> movimientos = movimientoCajaService.buscarPorCaja(cajaId);
        model.addAttribute("movimientos", movimientos);
        model.addAttribute("cajaId", cajaId);
        model.addAttribute("fechaInicio", inicio.toLocalDate());
        model.addAttribute("fechaFin", fin.toLocalDate());
        model.addAttribute("titulo", "Movimientos de Caja");

        // Resumen de movimientos
        if (!model.containsAttribute("resumen")) {
            model.addAttribute("resumen", Map.of("ingresos", 0.0, "egresos", 0.0, "saldo", 0.0));
        }

        return "movimientos/movimientos_caja";
    }

    // Exportar movimientos de caja a CSV (endpoint ejemplo)
    @GetMapping("/exportar")
    public void exportarCsv(@RequestParam("cajaId") Long cajaId, javax.servlet.http.HttpServletResponse response)
            throws IOException {
        List<MovimientoCaja> movimientos = movimientoCajaService.buscarPorCaja(cajaId);
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=movimientos_caja_" + cajaId + ".csv");

        try (java.io.PrintWriter writer = response.getWriter()) {
            writer.println("Fecha,Tipo,Monto,Descripción,Usuario");

            for (MovimientoCaja mov : movimientos) {
                writer.printf("%s,%s,%s,%s,%s\n",
                        mov.getFecha(),
                        mov.getTipo(),
                        mov.getMonto(),
                        mov.getDescripcion(),
                        mov.getOperador() != null ? mov.getOperador().getNomb_usu() : "-");
            }
            writer.flush();
        }
    }

    // Verificar si una caja tiene movimientos
    @GetMapping("/tiene-movimientos/{id}")
    @ResponseBody 
    public Map<String, Boolean> tieneMovimientos(@PathVariable("id") Long id) {
        Map<String, Boolean> response = new HashMap<>();
        Optional<Caja> cajaOpt = cajaService.obtenerCajaPorId(id); // Obtener la caja por ID
        if (cajaOpt.isPresent()) {
            List<MovimientoCaja> movimientos = movimientoCajaService.buscarPorCaja(id);
            response.put("tieneMovimientos", !movimientos.isEmpty());
        } else {
            response.put("tieneMovimientos", false); // Caja no encontrada o inactiva 
        }
        return response;
    }

    // Desactivar una caja (borrado lógico)
    @GetMapping("/eliminar/{id}") 
    public String desactivarCaja(@PathVariable("id") Long id, RedirectAttributes flash, Authentication authentication) {
        

        Optional<Caja> cajaOpt = cajaService.obtenerCajaPorId(id); 
        if (cajaOpt.isEmpty()) {
             flash.addFlashAttribute("error", "Caja no encontrada o ya está inactiva.");
             return "redirect:/caja/listado";
        }

        
        List<MovimientoCaja> movimientos = movimientoCajaService.buscarPorCaja(id);
        if (!movimientos.isEmpty()) {
            flash.addFlashAttribute("error", "Esta caja tiene movimientos. Use la opción de desactivación administrativa.");
            return "redirect:/caja/listado";
        }

        try {
            cajaService.desactivarCaja(id); 
            flash.addFlashAttribute("success", "Caja desactivada correctamente.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al desactivar la caja.");
            
        }
        return "redirect:/caja/listado";
    }

    @PostMapping("/desactivar-admin/{id}")
    @ResponseBody
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> desactivarCajaAdmin(@PathVariable("id") Long id, @RequestParam("password") String password, Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Usuario adminUsuario = usuarioService.buscarPorNombreUsuario(userDetails.getUsername())
                                    .orElse(null);

        if (adminUsuario == null) {
            response.put("success", false);
            response.put("message", "Error: Administrador no encontrado.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        if (!passwordEncoder.matches(password, adminUsuario.getClave())) {
            response.put("success", false);
            response.put("message", "Contraseña de administrador incorrecta.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        Optional<Caja> cajaOpt = cajaService.obtenerCajaPorId(id); 
        if (cajaOpt.isEmpty()) {
             response.put("success", false);
             response.put("message", "Caja no encontrada o ya está inactiva.");
             return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        try {
            cajaService.desactivarCaja(id);
            response.put("success", true);
            response.put("message", "Caja desactivada correctamente por administrador.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al desactivar la caja: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    //@GetMapping("/eliminar/{id}")
    // public String eliminarCaja(@PathVariable("id") Long id, RedirectAttributes flash) {
    //     try {
    //         // Primero verificar si la caja tiene movimientos asociados
    //         // Esta es una simplificación. Una lógica más robusta podría ser necesaria
    //         // dependiendo de las reglas de negocio (ej. no permitir eliminar si hay
    //         // movimientos).
    //         // Por ahora, simplemente intentamos eliminar.
    //         cajaService.eliminarCaja(id);
    //         flash.addFlashAttribute("success", "Caja eliminada correctamente.");
    //     } catch (DataIntegrityViolationException e) {
    //         flash.addFlashAttribute("error",
    //                 "No se puede eliminar la caja porque tiene movimientos asociados o está referenciada en otras transacciones.");
    //         // Log the exception for server-side analysis
    //         // import org.slf4j.Logger;
    //         // import org.slf4j.LoggerFactory;
    //         // private static final Logger logger =
    //         // LoggerFactory.getLogger(CajaController.class);
    //         // logger.error("Error al intentar eliminar caja ID {}: {}", id,
    //         // e.getMessage());
    //     } catch (Exception e) {
    //         flash.addFlashAttribute("error", "Error al eliminar la caja.");
    //         // logger.error("Error general al intentar eliminar caja ID {}: {}", id,
    //         // e.getMessage());
    //     }
    //     return "redirect:/caja/listado";
    // }
}