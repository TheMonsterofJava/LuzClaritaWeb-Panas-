package com.analistas.luzclaritaweb.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/inicioSesion")
public class LoginController {
    @GetMapping("/login")
    public String login(HttpServletRequest request, Model model) {
        String errorMessage = (String) request.getSession().getAttribute("error");
        if (errorMessage != null) {
            model.addAttribute("error", errorMessage);
            request.getSession().removeAttribute("error");
        }
        return "inicioSesion/login";
    }
}