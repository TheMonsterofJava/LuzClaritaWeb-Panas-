package com.analistas.luzclaritaweb.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import com.analistas.luzclaritaweb.model.domain.Permiso;
import com.analistas.luzclaritaweb.model.domain.Usuario;
//import com.analistas.luzclaritaweb.model.repository.IUsuarioRepository;
import com.analistas.luzclaritaweb.model.service.IUsuariosService;
import org.springframework.web.bind.annotation.RequestParam;





@Controller
@RequestMapping("/admin")
public class DashboardController {

    
     @Autowired
     private IUsuariosService usuarioService;

    //  @Autowired
    //  private IUsuarioRepository usuarioRepository;

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

    @GetMapping("user-management-add-user")
    public String addprofile(Model model) {
        return "admin/user-management-add-user";
    }

    @GetMapping("user-management-groups-list")
    public String addgroup(Model model){
        return "admin/user-management-groups-list";
    }

    public String getMethodName(@RequestParam String param) {
        return new String();
    }
    

    @GetMapping("/user-management-list")
    public String listprofile(Model model) {
        List<Usuario> usuarios = usuarioService.findAll();
        System.out.println("Usuarios encontrados: " + usuarios.size());
        usuarios.forEach(u -> System.out.println("Usuario: " + u.getNomb_usu()));
        model.addAttribute("usuarios", usuarios);
        return "admin/user-management-list";
    }

    //Editar usuarios desde la base de datos:
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
    
}


