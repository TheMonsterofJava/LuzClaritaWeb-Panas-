package com.analistas.luzclaritaweb.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

        // Mostrar el formulario tradicional solo si no se está autenticando con OAuth2
        model.addAttribute("showTraditionalLogin", true);

        return "inicioSesion/login";
    }

    @PostMapping("/login")
    public String procesarInicioSesion(
            @RequestParam("emailOrUser") String emailOrUser,
            @RequestParam("password") String password,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes,
            Model model) {

        System.out.println("Procesando inicio de sesión...");

        // Validación básica de los parámetros
        if (emailOrUser == null || emailOrUser.isEmpty() || password == null || password.isEmpty()) {
            System.out.println("Campos vacíos. Redirigiendo a /inicioSesion/login con error...");
            redirectAttributes.addFlashAttribute("error", "Todos los campos son obligatorios");
            return "redirect:/inicioSesion/login";
        }

        // Buscar usuario por email/usuario y contraseña
        Usuario usuario = usuarioService.findByEmailOrUSerAndPassword(emailOrUser, password);

        if (usuario != null) {
            // Inicio de sesión exitoso: guardar en sesión
            request.getSession().setAttribute("usuarioActual", usuario);
            redirectAttributes.addFlashAttribute("success", "Inicio de sesión exitoso");
            return "redirect:/inicioSesion/login?success=Inicio+de+sesión+exitoso";
        } else {
            System.out.println("Inicio de sesión fallido. Redirigiendo a /inicioSesion/login con error...");
            redirectAttributes.addFlashAttribute("error", "Usuario o contraseña incorrectos");
            redirectAttributes.addFlashAttribute("emailOrUser", emailOrUser);
            return "redirect:/inicioSesion/login";
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
