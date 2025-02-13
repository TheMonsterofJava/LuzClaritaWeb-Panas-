package com.analistas.luzclaritaweb.web.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


import com.analistas.luzclaritaweb.model.domain.Usuario;
import com.analistas.luzclaritaweb.model.service.IUsuariosService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/inicioSesion")
public class LoginController {

    @Autowired
    private IUsuariosService usuarioService;

    // Mostrar el formulario de inicio de sesión
    @GetMapping("/login")
    public String mostrarFormularioLogin(Model model, HttpServletRequest request) {
        String errorMessage = (String) request.getSession().getAttribute("error");
        
        // Si existe un error previo en la sesión, se pasa al modelo y se limpia
        if (errorMessage != null) {
            model.addAttribute("error", errorMessage);
            request.getSession().removeAttribute("error");
        }
        
        // Inicializar el atributo "emailOrUser" vacío
        model.addAttribute("emailOrUser", "");
        return "inicioSesion/login";
    }

    // Procesar los datos del formulario de inicio de sesión
    @PostMapping("/login")
    public String procesarInicioSesion(
            @RequestParam("emailOrUser") String emailOrUser, // Ajustado a camelCase
            @RequestParam("password") String password,
            HttpServletRequest request,
            Model model) {

        // Validación básica de los parámetros
        if (emailOrUser == null || emailOrUser.isEmpty() || password == null || password.isEmpty()) {
            model.addAttribute("error", "Todos los campos son obligatorios");
            model.addAttribute("emailOrUser", emailOrUser);
            return "inicioSesion/login";
        }

        // Buscar usuario por email/usuario y contraseña
        Usuario usuario = usuarioService.findByEmailOrUSerAndPassword(emailOrUser, password);

        if (usuario != null) {
            // Inicio de sesión exitoso: guardar en sesión y redirigir
            request.getSession().setAttribute("usuarioActual", usuario);
            return "redirect:/home";
        } else {
            // Inicio de sesión fallido: mostrar error
            model.addAttribute("emailOrUser", emailOrUser); // Preservar el valor ingresado
            model.addAttribute("error", "Usuario o contraseña incorrectos");
            return "inicioSesion/login";
        }
    }
}

    // @PostMapping("/login")
    // //implementar el UserOrEmail
    // public String login(@RequestParam("email") String email,
    // @RequestParam("nomb_usu") String nomb_usu,
    // @RequestParam("clave") String clave,
    // HttpServletRequest request,
    // Model model, Locale locale) {

    // Usuario usuario = usuarioService.findByUsernameEmailandPassword(email,
    // nomb_usu, clave);

    // if (usuario != null) {
    // request.getSession().setAttribute("usuarioActual", usuario);
    // return "redirect:/home";
    // } else {
    // String errorMessage;
    // if (locale != null) {
    // errorMessage = messageSource.getMessage("error.auth", null, locale);
    // } else {
    // errorMessage = messageSource.getMessage("error.auth", null,
    // Locale.getDefault()); // Usa el Locale por
    // // defecto
    // }
    // request.getSession().setAttribute("error", errorMessage);
    // return "redirect:/inicioSesion/login";
    // }

    // }

