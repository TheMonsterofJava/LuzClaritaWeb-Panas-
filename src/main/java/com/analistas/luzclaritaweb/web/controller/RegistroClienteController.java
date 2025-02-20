package com.analistas.luzclaritaweb.web.controller;

import lombok.RequiredArgsConstructor;


import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.analistas.luzclaritaweb.model.domain.Cliente;
import com.analistas.luzclaritaweb.model.domain.Permiso;
import com.analistas.luzclaritaweb.model.domain.Usuario;
import com.analistas.luzclaritaweb.model.service.IClienteService;
import com.analistas.luzclaritaweb.model.service.IUsuariosService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/registro")
public class RegistroClienteController {

    @Autowired
    private final IUsuariosService usuariosService;
    private final IClienteService clienteService; // Implementamos el Icliente Service
    private final PasswordEncoder passwordEncoder; // Inyectamos el PasswordEncoder

    @GetMapping
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("cliente", new Cliente());
        return "inicioSesion/login";
    }

    @PostMapping("/registro")
    public String procesarRegistroCliente( @ModelAttribute Cliente cliente,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model) {
        // Validar que las contraseñas coincidan
        if (!cliente.getContraseña().equals(confirmPassword)) {
            model.addAttribute("error", "Las contraseñas no coinciden.");
            return "inicioSesion/registro";
        }

        // Validar que el correo no esté ya registrado en Usuarios
        if (usuariosService.findByEmail(cliente.getCorreo()).isPresent()) {
            model.addAttribute("error", "El correo ya está registrado como usuario.");
            return "inicioSesion/registro";
        }

        // Encriptar la contraseña
        String contraseñaEncriptada = passwordEncoder.encode(cliente.getContraseña());
        cliente.setContraseña(contraseñaEncriptada);

        // Crear un nuevo Usuario asociado al Cliente
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setEmail(cliente.getCorreo());
        nuevoUsuario.setNomb_usu(cliente.getNomb_usu());
        nuevoUsuario.setClave(contraseñaEncriptada);
        nuevoUsuario.setFecha_creacion(new Date());
        nuevoUsuario.setActivo(true);

        // Asignar el permiso al Usuario (cliente)
        Permiso permisoCliente = usuariosService.obtenerPermisoPorNombre("Cliente");
        nuevoUsuario.setPermiso(permisoCliente);

        // Guardar el usuario en la base de datos
        Usuario usuarioGuardado = usuariosService.guardarUsuario(nuevoUsuario);

        // Asociar el Usuario al Cliente
        cliente.setUsuario(usuarioGuardado);

        // Guardar el cliente en la base de datos
        clienteService.guardarCliente(cliente);

        return "redirect:/inicioSesion/login";
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
