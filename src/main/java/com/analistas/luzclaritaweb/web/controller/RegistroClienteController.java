package com.analistas.luzclaritaweb.web.controller;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.analistas.luzclaritaweb.model.domain.Cliente;
import com.analistas.luzclaritaweb.model.domain.Usuario;
import com.analistas.luzclaritaweb.model.service.interfaces.IClienteService;
import com.analistas.luzclaritaweb.model.service.interfaces.IUsuariosService;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/registro")
public class RegistroClienteController {

    @Autowired
    private final IUsuariosService usuariosService;
    @Autowired
    @SuppressWarnings("unused")
    private final IClienteService clienteService;
    @Autowired
    private final PasswordEncoder passwordEncoder;

    @PostMapping
    @Transactional
    public String procesarRegistroCliente(
            @Valid @ModelAttribute("cliente") Cliente cliente,
            BindingResult result,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes,
            Model model) {

        // Validación de contraseñas
        if (!cliente.getContrasena().equals(confirmPassword)) {
            result.rejectValue("contrasena", "error.cliente", "Las contraseñas no coinciden");
            model.addAttribute("error", "Las contraseñas no coinciden");
            return "inicioSesion/login";
        }

        // Validación de email único
        if (usuariosService.findByEmail(cliente.getCorreo()).isPresent()) {
            result.rejectValue("correo", "error.cliente", "El correo ya está registrado");
            model.addAttribute("error", "El correo ya está registrado");
            return "inicioSesion/login";
        }

        if (result.hasErrors()) {
            return "inicioSesion/login";
        }

        try {
            // 1. Crear y configurar Usuario
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setEmail(cliente.getCorreo());
            nuevoUsuario.setNomb_usu(cliente.getNomb_usu());
            nuevoUsuario.setClave(passwordEncoder.encode(cliente.getContrasena()));
            nuevoUsuario.setFecha_creacion(new Date());
            nuevoUsuario.setActivo(true);
            nuevoUsuario.setPermiso(usuariosService.obtenerPermisoPorNombre("ROLE_CLIENTE"));

            // 2. Configurar Cliente y relación bidireccional
            cliente.setContrasena(passwordEncoder.encode(cliente.getContrasena()));
            nuevoUsuario.setCliente(cliente); // Método que maneja la relación bidireccional
            cliente.setUsuario(nuevoUsuario);

            // 3. Guardar SOLO el usuario (la cascada persiste el cliente)
            usuariosService.guardarUsuario(nuevoUsuario);

            // Mostrar un mensaje de exito cuando se registre un nuevo usuario
            redirectAttributes.addFlashAttribute("registroexitoso", "Registro exitoso. Por favor, inicie sesión.");
            return "redirect:/inicioSesion/login?login&registroexitoso=true";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al registrar: " + e.getMessage());
            return "redirect:/registro";
        }

    }

}

// @Controller
// @RequiredArgsConstructor
// @RequestMapping("/registro")
// public class RegistroClienteController {

// private final IClienteService clienteService;
// private final PasswordEncoder passwordEncoder; // Inyectamos el
// PasswordEncoder

// @GetMapping
// public String mostrarFormularioRegistro(Model model) {
// model.addAttribute("cliente", new Cliente());
// return "inicioSesion/login"; // Asegúrate de que este HTML tenga el
// formulario de registro
// }

// @PostMapping
// public String procesarRegistroCliente(@ModelAttribute Cliente cliente,
// @RequestParam("confirmPassword") String confirmPassword,
// Model model) {
// if (!cliente.getContraseña().equals(confirmPassword)) {
// model.addAttribute("error", "Las contraseñas no coinciden.");
// return "inicioSesion/login";
// }

// if (clienteService.findByCorreo(cliente.getCorreo()).isPresent()) {
// model.addAttribute("error", "El correo ya está registrado.");
// return "inicioSesion/login";
// }

// // Encriptar la contraseña antes de guardar
// cliente.setContraseña(passwordEncoder.encode(cliente.getContraseña()));
// clienteService.guardarCliente(cliente);
// return "redirect:/inicioSesion/login";
// }
// }