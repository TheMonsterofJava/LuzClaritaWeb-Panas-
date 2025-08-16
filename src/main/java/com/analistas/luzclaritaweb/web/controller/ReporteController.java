package com.analistas.luzclaritaweb.web.controller;

import com.analistas.luzclaritaweb.model.domain.RegistroVenta;
import com.analistas.luzclaritaweb.model.service.interfaces.IRegistroVentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/reportes")
@PreAuthorize("hasRole('ROLE_ADMIN')") // Aseguramos que solo los administradores puedan acceder
public class ReporteController {

    @Autowired
    private IRegistroVentaService registroVentaService;

    @GetMapping("/ventas")
    public String verReporteVentas(Model model) {
        List<RegistroVenta> ventas = registroVentaService.buscarTodos();

        // Calcular el total general de los ingresos
        BigDecimal totalGeneral = ventas.stream()
                                        .map(RegistroVenta::getTotalVenta)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("ventas", ventas);
        model.addAttribute("totalGeneral", totalGeneral);
        model.addAttribute("titulo", "Reporte de Ingresos por Ventas Online");

        // El nombre de la vista que se renderizará
        return "reportes/ventas";
    }
}

