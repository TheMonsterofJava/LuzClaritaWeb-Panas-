package com.analistas.luzclaritaweb.web.controller;

//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;

//import com.analistas.luzclaritaweb.model.domain.Usuario;
//import com.analistas.luzclaritaweb.model.service.IUsuariosService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/inicioSesion")
public class LoginController {

    // @Autowired
    // private IUsuariosService usuarioService;

    // Mostrar el formulario de inicio de sesión
    @GetMapping("/login")
    public String mostrarFormularioLogin(
            Model model,
            HttpServletRequest request,
            @RequestParam(value = "login", required = false) String login,
            @RequestParam(value = "registro", required = false) String registro,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "syncCart", required = false) String syncCart) {

        // Determinar qué formulario mostrar con base en los parámetros "registro" o
        // "login"
        boolean mostrarRegistro = (registro != null);
        boolean mostrarLogin = (login != null);

        model.addAttribute("mostrarRegistro", mostrarRegistro);
        model.addAttribute("showTraditionalLogin", mostrarLogin || !mostrarRegistro);

        // Pasar informacion de sincronizacion alñ carrito de compras
        if (syncCart != null) {
            model.addAttribute("syncCart", true);
        }

        // Spring Security maneja los errores automáticamente
        // pero podemos personalizar el mensaje si queremos
        if (error != null) {
            model.addAttribute("loginError", "Usuario o contraseña incorrectos");
        }

        return "inicioSesion/login";
    }
}
// String errorMessage = (String) request.getSession().getAttribute("error2");
// // No lo toma de aca al erro

// // Si existe un error previo en la sesión, se pasa al modelo y se limpia
// if (errorMessage != null) {
// model.addAttribute("error2", errorMessage);
// request.getSession().removeAttribute("error2"); // No toma de aca
// }

// //
// String successMessage = (String)
// request.getSession().getAttribute("success");
// if (successMessage != null) {
// model.addAttribute("success", successMessage);
// request.getSession().removeAttribute("success");
// }

// Inicializar el atributo "emailOrUser" vacío
// model.addAttribute("emailOrUser", "");

/**
 * ELIMINAR COMPLETAMENTE EL MÉTODO POST /login
 * Spring Security maneja esto automáticamente en /login (POST)
 * con los parámetros emailOrUser y password configurados en WebSecurityConfig
 */

// @PostMapping("/login")
// public String procesarInicioSesion(
// @RequestParam("emailOrUser") String emailOrUser,
// @RequestParam("password") String password,
// HttpServletRequest request,
// RedirectAttributes redirectAttributes,
// Model model) {

// System.out.println("Procesando inicio de sesión...");

// // Validación básica de los parámetros
// if (emailOrUser == null || emailOrUser.isEmpty() || password == null ||
// password.isEmpty()) {
// System.out.println("Campos vacíos. Redirigiendo a /inicioSesion/login con
// error...");
// redirectAttributes.addFlashAttribute("errorcamposvacios", "Todos los campos
// son obligatorios");
// return "redirect:/inicioSesion/login";
// }

// // Buscar usuario por email/usuario y contraseña
// Usuario usuario = usuarioService.findByEmailOrUSerAndPassword(emailOrUser,
// password);

// if (usuario != null) {
// // Inicio de sesión exitoso: guardar en sesión
// request.getSession().setAttribute("usuarioActual", usuario);
// redirectAttributes.addFlashAttribute("success", "Inicio de sesion exitoso");
// return "redirect:/home"; // Redirige al home directamente
// } else {
// System.out.println("Inicio de sesión fallido. Redirigiendo a
// /inicioSesion/login con error...");
// redirectAttributes.addFlashAttribute("errorusuarioycontraseña", "Usuario o
// contraseña incorrectos");
// redirectAttributes.addFlashAttribute("emailOrUser", emailOrUser);
// return "redirect:/inicioSesion/login";
// }
// }

// //
// String successMessage = (String)
// request.getSession().getAttribute("success");
// if (successMessage != null) {
// model.addAttribute("success", successMessage);
// request.getSession().removeAttribute("success");
// }
