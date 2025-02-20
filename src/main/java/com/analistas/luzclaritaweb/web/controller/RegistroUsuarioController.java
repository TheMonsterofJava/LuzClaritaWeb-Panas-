// package com.analistas.luzclaritaweb.web.controller;

// import org.springframework.stereotype.Controller;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.ModelAttribute;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;

// import com.analistas.luzclaritaweb.model.domain.Cliente;
// import com.analistas.luzclaritaweb.model.domain.Usuario;
// import com.analistas.luzclaritaweb.model.service.IClienteService;
// import com.analistas.luzclaritaweb.model.service.IUsuariosService;

// import org.springframework.ui.Model;
// import lombok.RequiredArgsConstructor;

// @Controller
// public class RegistroController {

//     @GetMapping("/registro")
//     public String mostrarFormularioRegistro() {
//         return "inicioSesion/login"; // La misma página que contiene el formulario
//     }

//     @PostMapping("/registro")
//     public String procesarRegistro(@RequestParam("nombre") String nombre,
//                                     @RequestParam("telefono") String telefono,
//                                     @RequestParam("email") String email,
//                                     @RequestParam("domicilio") String domicilio,
//                                     @RequestParam("password") String password,
//                                     @RequestParam("confirmPassword") String confirmPassword,
//                                     Model model) {
//         if (!password.equals(confirmPassword)) {
//             model.addAttribute("error", "Las contraseñas no coinciden");
//             return "inicioSesion/login";
//         }

//         // Aquí puedes realizar validaciones adicionales y guardar el usuario
//         // Supongamos que tienes un servicio `UsuarioService`
//         Usuario nuevoUsuario = new Usuario(nombre, telefono, email, domicilio, password);
//         IUsuariosService.guardarUsuario(nuevoUsuario);

//         // Redirigir al login después del registro exitoso
//         return "redirect:/inicioSesion/login";
//     }
// }


