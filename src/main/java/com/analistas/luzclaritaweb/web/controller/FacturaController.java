package com.analistas.luzclaritaweb.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.analistas.luzclaritaweb.model.domain.Factura;
import com.analistas.luzclaritaweb.model.domain.Usuario;
import com.analistas.luzclaritaweb.model.service.interfaces.IFacturaService;
import com.analistas.luzclaritaweb.web.config.security.CustomUserDetails;

@Controller
@RequestMapping("/facturas")
public class FacturaController {

    @Autowired
    private IFacturaService facturaService;

    @GetMapping("/mis-compras")
    public String verMisCompras(Model model, Authentication authentication) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/inicioSesion/login";
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Usuario usuario = userDetails.getUsuario();

        List<Factura> facturas = facturaService.buscarPorIdUsuario(usuario.getId());

        model.addAttribute("facturas", facturas);
        model.addAttribute("titulo", "Mis Compras");

        return "cliente/miscompras";
    }
}
