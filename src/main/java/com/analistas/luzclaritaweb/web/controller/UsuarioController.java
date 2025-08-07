package com.analistas.luzclaritaweb.web.controller;

import com.analistas.luzclaritaweb.web.config.security.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/usuario")
public class UsuarioController {

    @GetMapping("/actual")
    public ResponseEntity<?> obtenerUsuarioActual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("autenticado", false));
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Map<String, Object> response = new HashMap<>();
        response.put("autenticado", true);
        response.put("id", userDetails.getUserId());
        response.put("email", userDetails.getUsername());
        response.put("nombre", userDetails.getRealUsername());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/verificar")
    public ResponseEntity<Map<String, Boolean>> verificarAutenticacion() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean autenticado = authentication != null && authentication.isAuthenticated() && (authentication.getPrincipal() instanceof CustomUserDetails);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("autenticado", autenticado);

        return ResponseEntity.ok(response);
    }
}



    // Endpoint para obtener el usuario actual autenticado
    // Funciona con sesiones HTTP 
    //  @GetMapping("/actual")
    // public ResponseEntity<?> obtenerUsuarioActual(HttpSession session) {
    //     try {
    //         Usuario usuarioLogueado = (Usuario) session.getAttribute("usuario");
    //         Map<String, Object> response = new HashMap<>();
            
    //         if (usuarioLogueado != null) {
    //             response.put("autenticado", true);
    //             response.put("id", usuarioLogueado.getId());
    //             response.put("email", usuarioLogueado.getEmail());
    //             response.put("nombre", usuarioLogueado.getNomb_usu());
    //             return ResponseEntity.ok(response);
    //         } else {
    //             response.put("autenticado", false);
    //             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    //         }
    //     } catch (Exception e) {
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    //                 .body(Collections.singletonMap("error", "Error del servidor"));
    //     }
    // }

    // Endpoint para obtener el usuario actual autenticado
    // Funciona con Spring Security
    // Utiliza el objeto Usuario inyectado por Spring Security para obtener la información del usuario
    // @GetMapping("/actual")
    // public ResponseEntity<?> obtenerUsuarioActual(@AuthenticationPrincipal Usuario usuarioLogueado) {
    //     try {
    //         Map<String, Object> response = new HashMap<>();
            
    //         if (usuarioLogueado != null) {
    //             response.put("autenticado", true);
    //             response.put("id", usuarioLogueado.getId());
    //             response.put("email", usuarioLogueado.getEmail());
    //             response.put("nombre", usuarioLogueado.getNomb_usu());
    //             return ResponseEntity.ok(response);
    //         } else {
    //             response.put("autenticado", false);
    //             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    //         }
    //     } catch (Exception e) {
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    //                 .body(Collections.singletonMap("error", "Error del servidor"));
    //     }
    // }