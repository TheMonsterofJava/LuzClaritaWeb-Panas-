package com.analistas.luzclaritaweb.web.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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
import com.analistas.luzclaritaweb.model.domain.MovimientoCaja;
import com.analistas.luzclaritaweb.model.service.ICajaService;
import com.analistas.luzclaritaweb.model.service.IMovimientoCajaService;
import com.analistas.luzclaritaweb.model.service.IUsuariosService;

@Controller
@RequestMapping("/caja")
public class CajaController {

    @Autowired
    private ICajaService cajaService;

    @Autowired
    private IMovimientoCajaService movimientoCajaService;

    @Autowired
    @SuppressWarnings("unused")
    private IUsuariosService usuarioService;

    // Listar todas las cajas
    @GetMapping("/listado")
    public String listadoCajas(Model model) {
        List<Caja> cajas = cajaService.listarCajasAbiertas(); // Puedes cambiarlo por todas las cajas si lo prefieres
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
}