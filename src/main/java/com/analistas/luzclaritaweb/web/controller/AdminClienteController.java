// package com.analistas.luzclaritaweb.web.controller;

// import com.analistas.luzclaritaweb.model.domain.Cliente;
// import com.analistas.luzclaritaweb.model.domain.Usuario;
// import com.analistas.luzclaritaweb.model.domain.Permiso;
// import com.analistas.luzclaritaweb.model.service.IClienteService;
// import com.analistas.luzclaritaweb.model.service.IUsuariosService;
// import lombok.RequiredArgsConstructor;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.stereotype.Controller;
// import org.springframework.ui.Model;
// import org.springframework.web.bind.annotation.*;

// import java.util.Date;

// @Controller
// @RequestMapping("/clientes")
// @RequiredArgsConstructor
// public class AdminClienteController {

//     private final IClienteService clienteService;
//     private final IUsuariosService usuariosService;
//     private final BCryptPasswordEncoder passwordEncoder;

//     // Mostrar formulario de creación de clientes
//     @GetMapping("/crear")
//     public String mostrarFormularioCreacion(Model model) {
//         model.addAttribute("cliente", new Cliente());
//         model.addAttribute("permisos", usuariosService.findAllPermisos());
//         return "admin/user-management-add-user"; // Nombre del archivo HTML
//     }

//     // Guardar cliente en la base de datos
//     @PostMapping("/guardar")
//     public String guardarCliente(@ModelAttribute("cliente") Cliente cliente,
//                                  @RequestParam("permisoId") Long permisoId) {
//         // Buscar permiso por ID
//         Permiso permiso = (Permiso) usuariosService.findPermisoById(permisoId);
//         if (permiso != null) {

//             // Encriptar la contraseña
//             String contraseñaEncriptada = passwordEncoder.encode(cliente.getContraseña());
//             cliente.setContraseña(contraseñaEncriptada);

//             // Crear usuario relacionado al cliente
//             Usuario nuevoUsuario = new Usuario();
//             nuevoUsuario.setEmail(cliente.getCorreo());
//             nuevoUsuario.setNombre(cliente.getNomb_ape());
//             nuevoUsuario.setClave(contraseñaEncriptada);
//             nuevoUsuario.setFecha_creacion(new Date());
//             nuevoUsuario.setActivo(true);

//             // Asignar el permiso al Usuario (cliente)
//             Permiso permisoCliente = usuariosService.obtenerPermisoPorNombre("4 - CLIENTE");
//             nuevoUsuario.setPermiso(permisoCliente);

//             // Guardar el usuario en la base de datos
//             Usuario usuarioGuardado = usuariosService.guardarUsuario(nuevoUsuario);

//             // Asociar el Usuario al Cliente
//             cliente.setUsuario(usuarioGuardado);

//             // Guardar el cliente en la base de datos
//             clienteService.guardarCliente(cliente);

//         } else {
//             // Manejar el caso en que el permiso no sea encontrado
//             return "redirect:/clientes/crear?error=PermisoNoEncontrado";
//         }
//         return "redirect:/clientes/listar"; // Redirigir al listado de clientes
//     }

//     // Listar clientes
//     @GetMapping("/listar")
//     public String listarClientes(Model model) {
//         model.addAttribute("clientes", clienteService.findAll());
//         return "admin/user-management-list"; // Nombre del archivo HTML para listar clientes
//     }
// }

