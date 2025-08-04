// package com.analistas.luzclaritaweb.web.controller;

// import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.AuthenticationException;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.RestController;

// //Controlador para pruebas de autenticación
// // Permite probar la autenticación con un usuario y contraseña específicos
// @RestController
// @RequestMapping("/test")
// public class TestAuthController {

//     private final AuthenticationManager authenticationManager;

//     public TestAuthController(AuthenticationManager authenticationManager) {
//         this.authenticationManager = authenticationManager;
//     }

//     @PostMapping("/auth")
//     public String testAuth(@RequestParam String user, @RequestParam String pass) {
//         try {
//             Authentication auth = authenticationManager.authenticate(
//                 new UsernamePasswordAuthenticationToken(user, pass)
//             );
//             return "Autenticado" + auth.getName() + " - Roles: " + auth.getAuthorities();
//         } catch (AuthenticationException e) {
//             return "Error: " + e.getMessage();
//         }
//     }
    

// }
