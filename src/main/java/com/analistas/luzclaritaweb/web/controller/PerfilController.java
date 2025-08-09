package com.analistas.luzclaritaweb.web.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.analistas.luzclaritaweb.model.domain.Cliente;
import com.analistas.luzclaritaweb.model.domain.Usuario;
import com.analistas.luzclaritaweb.model.service.interfaces.IUsuariosService;
import com.analistas.luzclaritaweb.web.config.security.CustomUserDetails;

//Controlador para el perfil personal del usuario...
//ME FALTO LO DE LA CONTRASEÑA.... 
//REVISAR DESPUES
@Controller
public class PerfilController {

    private final IUsuariosService usuarioService;

    public PerfilController(IUsuariosService usuarioService) {
        this.usuarioService = usuarioService;

    }

    @GetMapping("/perfil")
    public String verPerfil(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Siempre obtener la última versión del usuario de la BD
        Usuario usuario = usuarioService.findById(userDetails.getUsuario().getId())
                .orElseThrow(() -> new RuntimeException(
                        "Usuario no encontrado con ID: " + userDetails.getUsuario().getId()));

        // Si el modelo no contiene ya un usuario (por un error de redirección), lo
        // añadimos.
        if (!model.containsAttribute("usuario")) {
            model.addAttribute("usuario", usuario);
        }
        return "admin/perfil";
    }

    @PostMapping("/perfil/info")
    public String actualizarInfoPersonal(@ModelAttribute("usuario") Usuario usuarioForm,
            RedirectAttributes redirectAttributes,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        try {
            Usuario usuarioActual = usuarioService.findById(userDetails.getUsuario().getId())
                    .orElseThrow(
                            () -> new IllegalStateException("No se pudo encontrar el usuario actual para actualizar."));

            // Asegurarse que el cliente existe...
            if (usuarioActual.getCliente() == null) {
                usuarioActual.setCliente(new Cliente());
            }

            usuarioActual.getCliente().setNomb_ape(usuarioForm.getCliente().getNomb_ape());
            usuarioActual.getCliente().setCelular(usuarioForm.getCliente().getCelular());
            usuarioActual.getCliente().setDireccion(usuarioForm.getCliente().getDireccion());

            usuarioService.updatePersonalInfo(usuarioActual.getCliente());

            // Sweet Alert para el exito...
            redirectAttributes.addFlashAttribute("successMessage",
                    "¡Tu información personal ha sido actualizada con éxito!");

        } catch (Exception e) {
            // Sweet Alert para el error
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ocurrió un error inesperado al actualizar tu información."
                            + e.getMessage());
        }

        return "redirect:/perfil";
    }

    @PostMapping("/perfil/password")
    public String cambiarPassword(
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (newPassword == null || newPassword.isEmpty() || !newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Las contraseñas nuevas no coinciden o están vacías.");
            return "redirect:/perfil";
        }

        // Añadir validación de complejidad si se desea (ej. longitud mínima)
        if (newPassword.length() < 5) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "La nueva contraseña debe tener al menos 5 caracteres.");
            return "redirect:/perfil";
        }

        try {
            // Obtenemos la entidad de usuario completa desde la BD
            Usuario usuario = usuarioService.findById(userDetails.getUsuario().getId())
                    .orElseThrow(() -> new IllegalStateException("Usuario no encontrado."));

            usuarioService.changePassword(usuario, currentPassword, newPassword);

            // redirectAttributes.addFlashAttribute("successpasswordParam", "¡Tu contraseña
            // ha sido cambiada con éxito!");
            // return "redirect:/inicioSesion/login?passwordUpdated=true";
            // SweetAlert para éxito
            redirectAttributes.addFlashAttribute("successMessage",
                    "¡Contraseña actualizada! Serás redirigido para iniciar sesión");

            return "redirect:/logout";// Redirige al usuario para iniciar sesión con la nueva contraseña

        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ocurrió un error inesperado al cambiar tu contraseña.");

        }
        return "redirect:/perfil";
    }

    @PostMapping("/perfil/actualizar-cuenta")
    public String actualizarInformacionCuenta(@RequestParam String nombUsu,
            @RequestParam String email,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        Usuario usuario = userDetails.getUsuario();

        try {
            usuarioService.updateAccountInfo(usuario, nombUsu, email);
            redirectAttributes.addFlashAttribute("successMessage",
                    "¡Tu nombre de usuario o email han sido actualizados! Por favor, vuelve a iniciar sesión para que los cambios surtan efecto.");
            return "redirect:/inicioSesion/login?accountUpdated=true";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/perfil";
        }
    }

}
