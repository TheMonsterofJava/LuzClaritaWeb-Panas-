package com.analistas.luzclaritaweb.web.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.analistas.luzclaritaweb.model.domain.Usuario;
import com.analistas.luzclaritaweb.model.repository.IMovimientoCajaRepository;
import com.analistas.luzclaritaweb.model.service.IMovimientoCajaService;

@Controller
@RequestMapping("/caja")
public class MovimientoCajaController {

    @Autowired
    private IMovimientoCajaService movimientoService;

    @Autowired
    private IMovimientoCajaRepository movimientoRepository;
    
    @GetMapping("/movimientos")
    public String listarMovimientos(
            @RequestParam(name = "fechaInicio", required = false) LocalDate fechaInicio,
            @RequestParam(name = "fechaFin", required = false) LocalDate fechaFin,
            @AuthenticationPrincipal Usuario usuario,
            Model model) {
        
        // Si no se especifican fechas, usar el mes actual
        if (fechaInicio == null) {
            fechaInicio = LocalDate.now().withDayOfMonth(1);
        }
        if (fechaFin == null) {
            fechaFin = LocalDate.now();
        }
        
        // Convertir LocalDate a LocalDateTime para las consultas
        LocalDateTime fechaInicioDT = fechaInicio.atStartOfDay();
        LocalDateTime fechaFinDT = fechaFin.atTime(LocalTime.MAX);
        
        // Obtener datos del servicio
        Map<String, Double> resumen = movimientoService.getResumenMovimientos(fechaInicioDT, fechaFinDT);
        //Double saldoActual = movimientoService.getSaldoActual();
        
        model.addAttribute("titulo", "Movimientos de Caja");
        model.addAttribute("movimientos", movimientoRepository.findByFechaBetween(fechaInicioDT, fechaFinDT));
        model.addAttribute("resumen", resumen);
        //model.addAttribute("saldoActual", saldoActual);
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);
        
        return "movimientos/movimientos_caja";
    }
}