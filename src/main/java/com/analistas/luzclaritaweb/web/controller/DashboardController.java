package com.analistas.luzclaritaweb.web.controller;

import java.security.Principal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.analistas.luzclaritaweb.model.domain.Cliente;
import com.analistas.luzclaritaweb.model.domain.Permiso;
import com.analistas.luzclaritaweb.model.domain.Usuario;
import com.analistas.luzclaritaweb.model.service.IClienteService;
import com.analistas.luzclaritaweb.model.service.IUsuariosService;

import jakarta.transaction.Transactional;

@Controller
@RequestMapping("/admin")
@Secured({"ROLE_ADMIN"}) // Aseguramos que solo los usuarios con rol de administrador puedan acceder a estas rutas
public class DashboardController {

    @Autowired
    private IUsuariosService usuarioService;

    @Autowired
    private IClienteService clienteService;

    // @Autowired
    // private IUsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/dashboard-1")
    public String dashboardamin(Model model) {
        return "admin/dashboard-1";
    }

    @GetMapping("/account-profile")
    public String acountprofile(Model model) {
        return "admin/account-profile";
    }

    @GetMapping("user-management-edit-user")
    public String editprofile(Model model) {
        return "admin/user-management-edit-user";
    }

    @GetMapping("user-management-groups-list")
    public String addgroup(Model model) {
        return "admin/user-management-groups-list";
    }

    public String getMethodName(@RequestParam String param) {
        return new String();
    }

    @GetMapping("/user-management-list")
    public String listprofile(Model model, CsrfToken csrfToken) {
        // Añadir el token CSRF al modelo
        List<Usuario> usuarios = usuarioService.findAll();

        // Verificar si la lista tiene al menos 5 elementos
        int limit = Math.min(usuarios.size(), 5); // Limita a 5 usuarios o al tamaño de la lista, lo que sea menor
        List<Usuario> usuariosLimitados = usuarios.subList(0, limit);

        System.out.println("Usuarios encontrados: " + usuariosLimitados.size());
        usuariosLimitados.forEach(u -> System.out.println("Usuario: " + u.getNomb_usu()));

        model.addAttribute("usuarios", usuariosLimitados);
        model.addAttribute("_csrf", csrfToken); // <-- Añadir el token CSRF al modelo
        return "admin/user-management-list";
    }

    // Metodo para añadir un nuevo usuario
    // Método para mostrar formulario de añadir usuario
    @GetMapping("/user-management-add-user")
    public String showAddUserForm(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("permisos", usuarioService.findAllPermisos());
        return "admin/user-management-add-user";
    }

    // Método para procesar añadir usuario
    @PostMapping("/user-management-add-user")
    public String addUser(@ModelAttribute("usuario") Usuario usuario,
            @RequestParam(value = "crearCliente", required = false) boolean crearCliente,
            RedirectAttributes redirectAttributes) {
        try {
            // 1. Encriptar la contraseña
            String contraseñaEncriptada = passwordEncoder.encode(usuario.getClave());
            usuario.setClave(contraseñaEncriptada);

            // 2. Configurar datos básicos del usuario
            usuario.setFecha_creacion(new Date());
            usuario.setActivo(true);

            // 3. Obtener y asignar permiso
            Permiso permiso = (Permiso) usuarioService.findPermisoById(usuario.getPermiso().getId());
            usuario.setPermiso(permiso);

            // 4. Guardar el usuario primero
            Usuario usuarioGuardado = usuarioService.guardarUsuario(usuario);

            // 5. Si es cliente, crear registro correspondiente
            if (crearCliente && "ROLE_CLIENTE".equals(usuario.getPermiso().getNombre())) {
                Cliente cliente = new Cliente();
                cliente.setNomb_ape(usuario.getNomb_usu());
                cliente.setNomb_usu(usuario.getNomb_usu());
                cliente.setCorreo(usuario.getEmail());
                cliente.setContrasena(contraseñaEncriptada); // Misma contraseña encriptada
                cliente.setUsuario(usuarioGuardado);

                // Establecer relación bidireccional
                usuarioGuardado.setCliente(cliente);

                // Guardar ambos
                clienteService.guardarCliente(cliente);
                usuarioService.guardarUsuario(usuarioGuardado);
            }

            redirectAttributes.addFlashAttribute("success", "Usuario agregado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al agregar el usuario: " + e.getMessage());
        }
        return "redirect:/admin/user-management-list";
    }

    

    // Editar usuarios desde la base de datos:
    @GetMapping("/user-management-edit-user/{id}")
    public String editUsuario(@PathVariable("id") Long id, Model model) {
        try {
            // Buscamos el usuario por ID
            Usuario usuario = usuarioService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

            // Agregamos el usuario al modelo
            model.addAttribute("usuario", usuario);

            // También agregamos la lista de permisos para el selector de roles
            List<Permiso> permisos = usuarioService.findAllPermisos();
            model.addAttribute("permisos", permisos);

            System.out.println("Cargando usuario para editar: " + usuario.getNomb_usu());

            return "admin/user-management-edit-user";

        } catch (Exception e) {
            System.err.println("Error al cargar usuario: " + e.getMessage());
            // Podrías agregar un mensaje de error al modelo aquí
            return "redirect:/admin/user-management-list";
        }
    }

    // Método para procesar la edición del usuario - junto con la contraseña actual
    @PostMapping("/user-management-edit-user/{id}")
    public String updateUsuario(@PathVariable("id") Long id,
            @ModelAttribute("usuario") Usuario usuario,
            @RequestParam(value = "nuevaClave", required = false) String nuevaClave,
            @RequestParam(value = "confirmarNuevaClave", required = false) String confirmarNuevaClave,
            RedirectAttributes redirectAttributes) {
        try {
            // Buscamos el usuario existente por ID
            Usuario usuarioExistente = usuarioService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

            // Actualizamos los campos del usuario existente
            usuarioExistente.setNomb_usu(usuario.getNomb_usu());
            usuarioExistente.setEmail(usuario.getEmail());

            // Si se proporciona una nueva contraseña y coincide con la confirmación, la
            // hasheamos
            if (nuevaClave != null && !nuevaClave.isEmpty() && nuevaClave.equals(confirmarNuevaClave)) {
                String contraseñaHasheada = passwordEncoder.encode(nuevaClave);
                usuarioExistente.setClave(contraseñaHasheada);
            } else if (nuevaClave != null && !nuevaClave.isEmpty()) {
                // Si las contraseñas no coinciden, mostramos un mensaje de error
                redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden.");
                return "redirect:/admin/user-management-edit-user/" + id;
            }

            // Actualizamos el permiso
            Permiso permiso = (Permiso) usuarioService.findPermisoById(usuario.getPermiso().getId());
            usuarioExistente.setPermiso(permiso);

            // Guardamos el usuario actualizado en la base de datos
            usuarioService.guardarUsuario(usuarioExistente);

            // Agregar mensaje de éxito
            redirectAttributes.addFlashAttribute("success", "Usuario actualizado correctamente.");
        } catch (Exception e) {
            // Agregar mensaje de error
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el usuario: " + e.getMessage());
        }
        return "redirect:/admin/user-management-list";
    }

    // Método para eliminar usuarios - sin contraseña del administrador o
    // programador
    // Método para eliminar usuarios
    @PostMapping("/usuarios/eliminar/{id}")
    @ResponseBody
    @Transactional
    public Map<String, Object> eliminarUsuario(@PathVariable("id") Long id, Principal principal) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 1. Verificar permisos del usuario actual
            String username = principal.getName();
            Optional<Usuario> usuarioActualOpt = usuarioService.findByNombUsu(username);
            
            if (!usuarioActualOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Usuario no encontrado.");
                return response;
            }

            Usuario usuarioActual = usuarioActualOpt.get();
            String permisoActual = usuarioActual.getPermiso().getNombre();
            
            if (!permisoActual.equals("ROLE_ADMIN")) { //&& !permisoActual.equals("ROlE_PROGRAMADOR")) por si queremos poner otro rol
                response.put("success", false);
                response.put("message", "No tienes permisos para eliminar usuarios.");
                return response;
            }

            // 2. Eliminar usuario (y cliente asociado automáticamente por cascade)
            usuarioService.eliminarUsuario(id);
            
            response.put("success", true);
            response.put("message", "Usuario eliminado correctamente.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al eliminar el usuario: " + e.getMessage());
        }
        
        return response;
    }

    // @GetMapping("/user-management-add-user")
    // public String showAddUserForm(Model model) {
    // model.addAttribute("usuario", new Usuario());
    // model.addAttribute("permisos", usuarioService.findAllPermisos());
    // return "admin/user-management-add-user";
    // }

    // @PostMapping("/user-management-add-user")
    // public String addUser(@ModelAttribute("usuario") Usuario usuario,
    // RedirectAttributes redirectAttributes) {
    // try {
    // // Hashear la contraseña
    // String contraseñaHasheada = passwordEncoder.encode(usuario.getClave());
    // usuario.setClave(contraseñaHasheada);

    // // Obtener el permiso seleccionado
    // Permiso permiso = (Permiso)
    // usuarioService.findPermisoById(usuario.getPermiso().getId());
    // usuario.setPermiso(permiso);

    // // Establecer la fecha de creación y activar el usuario
    // usuario.setFecha_creacion(new Date());
    // usuario.setActivo(true);

    // // Guardar el usuario en la base de datos
    // usuarioService.guardarUsuario(usuario);

    // // Agregar mensaje de éxito
    // redirectAttributes.addFlashAttribute("success", "Usuario agregado
    // correctamente.");
    // } catch (Exception e) {
    // // Agregar mensaje de error
    // redirectAttributes.addFlashAttribute("error", "Error al agregar el usuario: "
    // + e.getMessage());
    // }
    // return "redirect:/admin/user-management-list";
    // }


    // @PostMapping("/usuarios/eliminar/{id}")
    // @ResponseBody
    // public Map<String, Object> eliminarUsuario(@PathVariable("id") Long id, Principal principal) {
    //     Map<String, Object> response = new HashMap<>();
    //     System.out.println("Solicitud de eliminación recibida para el usuario con ID: " + id);

    //     try {
    //         String username = principal.getName(); // Obtener el nombre de usuario actual
    //         System.out.println("Usuario actual: " + username);

    //         // Buscar el usuario actual por nombre de usuario (nomb_usu)
    //         Optional<Usuario> usuarioActualOpt = usuarioService.findByNombUsu(username);
    //         if (!usuarioActualOpt.isPresent()) {
    //             System.out.println("Usuario no encontrado con nombre de usuario: " + username);
    //             response.put("success", false);
    //             response.put("message", "Usuario no encontrado.");
    //             return response;
    //         }

    //         Usuario usuarioActual = usuarioActualOpt.get();
    //         System.out.println("Permiso del usuario actual: " + usuarioActual.getPermiso().getNombre());

    //         // Verificar permisos (Administrador o Programador)
    //         if (!usuarioActual.getPermiso().getNombre().equals("Administrador") &&
    //                 !usuarioActual.getPermiso().getNombre().equals("Programador")) {
    //             System.out.println("El usuario no tiene permisos para eliminar.");
    //             response.put("success", false);
    //             response.put("message", "No tienes permisos para eliminar usuarios.");
    //             return response;
    //         }

    //         // Eliminar el usuario
    //         System.out.println("Eliminando usuario con ID: " + id);
    //         usuarioService.eliminarUsuario(id);
    //         response.put("success", true);
    //         response.put("message", "Usuario eliminado correctamente.");
    //     } catch (Exception e) {
    //         System.err.println("Error al eliminar el usuario: " + e.getMessage());
    //         response.put("success", false);
    //         response.put("message", "Error al eliminar el usuario: " + e.getMessage());
    //     }

    //     return response;
    // }

}

// @PostMapping("/usuarios/eliminar/{id}")
// @ResponseBody
// public Map<String, Object> eliminarUsuario(@PathVariable("id") Long id,
// Principal principal) {
// Map<String, Object> response = new HashMap<>();
// System.out.println("Solicitud de eliminación recibida para el usuario con ID:
// " + id);

// try {
// String username = principal.getName();
// System.out.println("Usuario actual: " + username);

// // Buscar el usuario actual por email
// Optional<Usuario> usuarioActualOpt = usuarioService.findByEmail(username);
// if (!usuarioActualOpt.isPresent()) {
// System.out.println("Usuario no encontrado con email: " + username);
// response.put("success", false);
// response.put("message", "Usuario no encontrado.");
// return response;
// }

// Usuario usuarioActual = usuarioActualOpt.get();
// System.out.println("Permiso del usuario actual: " +
// usuarioActual.getPermiso().getNombre());

// // Verificar permisos (Administrador o Programador)
// if (!usuarioActual.getPermiso().getNombre().equals("Administrador") &&
// !usuarioActual.getPermiso().getNombre().equals("Programador")) {
// System.out.println("El usuario no tiene permisos para eliminar.");
// response.put("success", false);
// response.put("message", "No tienes permisos para eliminar usuarios.");
// return response;
// }

// // Eliminar el usuario
// System.out.println("Eliminando usuario con ID: " + id);
// usuarioService.eliminarUsuario(id);
// response.put("success", true);
// response.put("message", "Usuario eliminado correctamente.");
// } catch (Exception e) {
// System.err.println("Error al eliminar el usuario: " + e.getMessage());
// response.put("success", false);
// response.put("message", "Error al eliminar el usuario: " + e.getMessage());
// }

// return response;
// }