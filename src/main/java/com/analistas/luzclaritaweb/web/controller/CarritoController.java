package com.analistas.luzclaritaweb.web.controller;

import java.util.Collections; // Asegúrate de importar Collections
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired; // Importar ResponseEntity
import org.springframework.http.HttpStatus; // Importar AuthenticationPrincipal
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.analistas.luzclaritaweb.model.domain.Usuario;
import com.analistas.luzclaritaweb.model.service.interfaces.ICarritoService;
import com.analistas.luzclaritaweb.dto.CarritoDTO; // Importar CarritoDTO


@RestController
@RequestMapping("/api/carrito")
@CrossOrigin("*")
public class CarritoController {

    @Autowired
    private ICarritoService carritoService;

    // Modificado para usar AuthenticationPrincipal y devolver DTOs
    @GetMapping
    public ResponseEntity<List<CarritoDTO>> obtenerCarrito(@AuthenticationPrincipal Usuario usuarioAutenticado) {
        if (usuarioAutenticado == null) {
            // Usuario no logueado, devuelve carrito vacío, lo cual es correcto.
            return ResponseEntity.ok(Collections.emptyList());
        }
        List<CarritoDTO> carrito = carritoService.obtenerCarritoPorUsuario(usuarioAutenticado.getId());
        return ResponseEntity.ok(carrito);
    }

    // Modificado para usar AuthenticationPrincipal
    @PostMapping("/agregar")
    public ResponseEntity<Void> agregarProducto(
            @AuthenticationPrincipal Usuario usuarioAutenticado,
            @RequestParam Long productoId,
            @RequestParam int cantidad) {
        if (usuarioAutenticado == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // O FORBIDDEN si tiene más sentido
        }
        carritoService.agregarProducto(usuarioAutenticado, productoId, cantidad);
        return ResponseEntity.ok().build();
    }

    // Modificado para usar AuthenticationPrincipal
    @DeleteMapping("/eliminar")
    public ResponseEntity<Void> eliminarProducto(@AuthenticationPrincipal Usuario usuarioAutenticado,
            @RequestParam Long productoId) {
        if (usuarioAutenticado == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        carritoService.eliminarProducto(usuarioAutenticado.getId(), productoId);
        return ResponseEntity.ok().build();
    }

    // Modificado para usar AuthenticationPrincipal
    @DeleteMapping("/vaciar") // Ya no necesita {usuarioId} en la URL
    public ResponseEntity<Void> vaciarCarrito(@AuthenticationPrincipal Usuario usuarioAutenticado) {
        if (usuarioAutenticado == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        carritoService.vaciarCarrito(usuarioAutenticado.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sincronizar")
    @SuppressWarnings("CallToPrintStackTrace")
    public ResponseEntity<?> sincronizarCarrito(
            @AuthenticationPrincipal Usuario usuarioAutenticado,
            @RequestBody List<ItemCarritoLocal> itemsLocal) {
        
        if (usuarioAutenticado == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Usuario no autenticado"));
        }

        try {
            carritoService.sincronizarDesdeLocalStorage(usuarioAutenticado, itemsLocal);
            return ResponseEntity.ok(Collections.singletonMap("success", "Carrito sincronizado correctamente"));
        } catch (Exception e) {
            // Log the exception for debugging purposes
            System.err.println("Error grave durante la sincronización del carrito: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Error interno al sincronizar el carrito."));
        }
    }

    //Clase para recibir los items del carrito desde el localStorage
    // Esta clase debe coincidir con la estructura de los objetos en el localStorage
    public static class ItemCarritoLocal {
        private Long id;
        private String nombre;
        private Double precio;
        private Integer cantidad;
        private String imagen;
        
        // Constructores
        public ItemCarritoLocal() {}
        
        // Getters y Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        
        public Double getPrecio() { return precio; }
        public void setPrecio(Double precio) { this.precio = precio; }
        
        public Integer getCantidad() { return cantidad; }
        public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
        
        public String getImagen() { return imagen; }
        public void setImagen(String imagen) { this.imagen = imagen; }
    }
    
}
//     @GetMapping("/api/usuario/actual")
//     @ResponseBody
//     public ResponseEntity<?> obtenerUsuarioActual(HttpSession session) {
//         try {
//             // Verificar si hay una sesión activa y un usuario logueado
//             Usuario usuarioLogueado = (Usuario) session.getAttribute("usuario");

//             if (usuarioLogueado != null) {
//                 // Crear un DTO con información básica del usuario (sin datos sensibles)
//                 Map<String, Object> usuarioInfo = new HashMap<>();
//                 usuarioInfo.put("id", usuarioLogueado.getId());
//                 usuarioInfo.put("email", usuarioLogueado.getEmail());
//                 usuarioInfo.put("nombre", usuarioLogueado.getNomb_usu());
//                 usuarioInfo.put("autenticado", true);

//                 return ResponseEntity.ok(usuarioInfo);
//             } else {
//                 // Usuario no autenticado
//                 return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                         .body(Collections.singletonMap("autenticado", false));
//             }
//         } catch (Exception e) {
//             // Log del error para debugging
//             System.err.println("Error al verificar usuario actual: " + e.getMessage());

//             // Retornar error 401 en caso de cualquier problema
//             return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                     .body(Collections.singletonMap("error", "No se pudo verificar el estado de autenticación"));
//         }
//     }

//     // Método alternativo más simple si solo necesitas verificar autenticación
//     @GetMapping("/api/usuario/verificar")
//     @ResponseBody
//     public ResponseEntity<Map<String, Boolean>> verificarAutenticacion(HttpSession session) {
//         Usuario usuarioLogueado = (Usuario) session.getAttribute("usuario");
//         boolean autenticado = usuarioLogueado != null;

//         Map<String, Boolean> response = new HashMap<>();
//         response.put("autenticado", autenticado);

//         if (autenticado) {
//             return ResponseEntity.ok(response);
//         } else {
//             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
//         }
//     }

// // Vamos a ver si corregimos el error

// // REGLA PARA EL NUEVO ENDPOINT
// // Permitimos que cualquiera intente acceder a esta URL.
// // Nuestro controlador devolverá 200 o 401 según el estado de la sesión.
// .antMatchers("/api/usuario/actual").permitAll()

// // REGLAS PARA EL CARRITO (Probablemente ya lo tienes)
// // Las APIs del carrito deben requerir autenticación.
// .antMatchers("/api/carrito/**").authenticated()

// Endopint para verificar el usuario actual:
// @GetMapping("/api/usuario/actual")
// public ResponseEntity<?> getActualUsuario(@AuthenticationPrincipal Usuario
// usuarioAutenticado) {

// if (usuarioAutenticado != null) {
// return ResponseEntity.ok().build(); // Usuario autenticado, puedes devolver
// más info si lo necesitas
// } else {
// return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // Usuario no
// logueado
// }

// }