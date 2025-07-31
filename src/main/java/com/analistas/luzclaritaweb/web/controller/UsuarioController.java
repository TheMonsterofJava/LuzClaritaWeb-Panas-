package com.analistas.luzclaritaweb.web.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.analistas.luzclaritaweb.model.domain.Usuario;
import com.analistas.luzclaritaweb.web.config.security.CustomUserDetails;



//Nuevo controlador para manejar las peticiones relacionadas con el usuario actual
//Este controlador permite obtener la información del usuario autenticado en la sesión actual
// Se espera que el usuario esté almacenado en la sesión bajo el atributo "usuario"
// Si el usuario está autenticado, se devuelve su ID, email y nombre; de lo contrario, se indica que no está autenticado
// En caso de error, se devuelve un mensaje de error en la respuesta
@RestController
@RequestMapping("/api/usuario")
public class UsuarioController {

    @GetMapping("/actual")
    public ResponseEntity<?> obtenerUsuarioActual(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Map<String, Object> response = new HashMap<>();
            
            if (userDetails != null) {
                response.put("autenticado", true);
                response.put("id", userDetails.getUserId());
                response.put("email", userDetails.getUsername());
                response.put("nombre", userDetails.getRealUsername());
                return ResponseEntity.ok(response);
            } else {
                response.put("autenticado", false);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Error del servidor"));
        }
    }

    /**
     * Endpoint alternativo para verificar solo el estado de autenticación
     */
    @GetMapping("/verificar")
    public ResponseEntity<Map<String, Boolean>> verificarAutenticacion(
            @AuthenticationPrincipal Usuario usuarioAutenticado) {
        
        boolean autenticado = usuarioAutenticado != null;
        Map<String, Boolean> response = new HashMap<>();
        response.put("autenticado", autenticado);

        if (autenticado) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
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