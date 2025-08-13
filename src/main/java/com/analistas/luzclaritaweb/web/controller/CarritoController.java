package com.analistas.luzclaritaweb.web.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.analistas.luzclaritaweb.dto.CarritoDTO;
import com.analistas.luzclaritaweb.model.domain.Usuario;
import com.analistas.luzclaritaweb.model.service.interfaces.ICarritoService;
import com.analistas.luzclaritaweb.web.config.security.CustomUserDetails;
import com.analistas.luzclaritaweb.web.excepciones.CarritoSyncException;

//ME FALTA PONER QUE LOS USUARIOS QUE NO ESTAN LOGUEADOS PUEDAN AUMENTAR LA CANTIDA DE PRODUCTOS AL CARRITO...
@RestController
@RequestMapping("/api/carrito")
@CrossOrigin("*")
public class CarritoController {

    @Autowired
    private ICarritoService carritoService;

    private Usuario getUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) authentication.getPrincipal()).getUsuario();
        }
        return null;
    }

    @GetMapping
    public ResponseEntity<List<CarritoDTO>> obtenerCarrito() {
        Usuario usuario = getUsuarioAutenticado();
        if (usuario == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        List<CarritoDTO> carrito = carritoService.obtenerCarritoPorUsuario(usuario.getId());
        return ResponseEntity.ok(carrito);
    }

    @PostMapping("/agregar")
    public ResponseEntity<?> agregarProducto(@RequestParam Long productoId, @RequestParam int cantidad) {
        Usuario usuario = getUsuarioAutenticado();
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Usuario no autenticado"));
        }
        try {
            carritoService.agregarProducto(usuario, productoId, cantidad);
            return ResponseEntity.ok().build();
        } catch (CarritoSyncException e) {
            System.err.println("Error de negocio/datos al agregar producto: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "No se pudo agregar el producto al carrito. Causa: " + e.getMessage()));
        }
    }

     @PostMapping("/agregarReceta")
    public ResponseEntity<?> agregarReceta(@RequestParam Long recetaId, @RequestParam int cantidad) {
        Usuario usuario = getUsuarioAutenticado();
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Usuario no autenticado"));
        }
        try {
            carritoService.agregarReceta(usuario, recetaId, cantidad);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "No se pudo agregar la receta al carrito. Causa: " + e.getMessage()));
        }
    }

    // Actualizar la cantidad de los productos en el carrito de compras:
    @PostMapping("/actualizar")
    public ResponseEntity<?> actualizarCantidad(@RequestParam Long productoId, @RequestParam int cantidad) {
        Usuario usuario = getUsuarioAutenticado();

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Usuario no autenticado"));
        }

        if (cantidad < 1) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "La cantidad debe ser mayor a 0."));
        }

        try {
            carritoService.actualizarCantidad(usuario, productoId, cantidad);
            return ResponseEntity.ok(Map.of("success", "Cantidad actualizada correctamente."));
        } catch (Exception e) {
            // Log the exception for debugging purposes
            System.err.println("Error al actualizar cantidad en el carrito: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno al actualizar la cantidad."));
        }
    }

    @DeleteMapping("/eliminar")
    public ResponseEntity<Void> eliminarProducto(@RequestParam Long productoId) {
        Usuario usuario = getUsuarioAutenticado();
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        carritoService.eliminarProducto(usuario.getId(), productoId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/vaciar")
    public ResponseEntity<Void> vaciarCarrito() {
        Usuario usuario = getUsuarioAutenticado();
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        carritoService.vaciarCarrito(usuario.getId());
        return ResponseEntity.ok().build();
    }

    // @PostMapping("/sincronizar")
    // public ResponseEntity<?> sincronizarCarrito(@RequestBody
    // List<ItemCarritoLocal> itemsLocal) {
    // Usuario usuario = getUsuarioAutenticado();
    // if (usuario == null) {
    // return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
    // .body(Map.of("error", "Usuario no autenticado"));
    // }

    // carritoService.sincronizarDesdeLocalStorage(usuario, itemsLocal);
    // return ResponseEntity.ok(Map.of("success", "Carrito sincronizado
    // correctamente"));
    // }

    @PostMapping("/sincronizar")
    public ResponseEntity<?> sincronizarCarrito(@RequestBody List<ItemCarritoLocal> itemsLocal) {
        Usuario usuario = getUsuarioAutenticado();
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Usuario no autenticado"));
        }

        try {
            carritoService.sincronizarDesdeLocalStorage(usuario, itemsLocal);
            return ResponseEntity.ok(Map.of(
                    "success", "Carrito sincronizado correctamente",
                    "itemsCount", itemsLocal.size()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error de sincronización: " + e.getMessage()));
        }
    }

    @ExceptionHandler(CarritoSyncException.class)
    @SuppressWarnings("CallToPrintStackTrace")
    public ResponseEntity<Map<String, String>> handleCarritoSyncException(CarritoSyncException e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error en el servidor al procesar el carrito: " + e.getMessage()));
    }

    public static class ItemCarritoLocal {
        private Long id;
        private String nombre;
        private Double precio;
        private Integer cantidad;
        private String imagen;

        public ItemCarritoLocal() {
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public Double getPrecio() {
            return precio;
        }

        public void setPrecio(Double precio) {
            this.precio = precio;
        }

        public Integer getCantidad() {
            return cantidad;
        }

        public void setCantidad(Integer cantidad) {
            this.cantidad = cantidad;
        }

        public String getImagen() {
            return imagen;
        }

        public void setImagen(String imagen) {
            this.imagen = imagen;
        }
    }
}
// @GetMapping("/api/usuario/actual")
// @ResponseBody
// public ResponseEntity<?> obtenerUsuarioActual(HttpSession session) {
// try {
// // Verificar si hay una sesión activa y un usuario logueado
// Usuario usuarioLogueado = (Usuario) session.getAttribute("usuario");

// if (usuarioLogueado != null) {
// // Crear un DTO con información básica del usuario (sin datos sensibles)
// Map<String, Object> usuarioInfo = new HashMap<>();
// usuarioInfo.put("id", usuarioLogueado.getId());
// usuarioInfo.put("email", usuarioLogueado.getEmail());
// usuarioInfo.put("nombre", usuarioLogueado.getNomb_usu());
// usuarioInfo.put("autenticado", true);

// return ResponseEntity.ok(usuarioInfo);
// } else {
// // Usuario no autenticado
// return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
// .body(Collections.singletonMap("autenticado", false));
// }
// } catch (Exception e) {
// // Log del error para debugging
// System.err.println("Error al verificar usuario actual: " + e.getMessage());

// // Retornar error 401 en caso de cualquier problema
// return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
// .body(Collections.singletonMap("error", "No se pudo verificar el estado de
// autenticación"));
// }
// }

// // Método alternativo más simple si solo necesitas verificar autenticación
// @GetMapping("/api/usuario/verificar")
// @ResponseBody
// public ResponseEntity<Map<String, Boolean>>
// verificarAutenticacion(HttpSession session) {
// Usuario usuarioLogueado = (Usuario) session.getAttribute("usuario");
// boolean autenticado = usuarioLogueado != null;

// Map<String, Boolean> response = new HashMap<>();
// response.put("autenticado", autenticado);

// if (autenticado) {
// return ResponseEntity.ok(response);
// } else {
// return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
// }
// }

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