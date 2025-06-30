// package com.analistas.luzclaritaweb.web.controller;
// import java.util.List;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.web.bind.annotation.CrossOrigin;
// import org.springframework.web.bind.annotation.DeleteMapping;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.RestController;

// import com.analistas.luzclaritaweb.model.domain.Carrito;
// import com.analistas.luzclaritaweb.model.service.ICarritoService;

// @RestController
// @RequestMapping("/api/carrito")
// @CrossOrigin("*")  // Permitir peticiones desde frontend
// public class CarritoController {

//     @Autowired
//     private ICarritoService carritoService;

//     @GetMapping("/{usuarioId}")
//     public List<Carrito> obtenerCarrito(@PathVariable Long usuarioId) {
//         return carritoService.obtenerCarritoPorUsuario(usuarioId);
//     }

//     @PostMapping("/agregar")
//     public void agregarProducto(@RequestParam Long usuarioId, @RequestParam Long productoId, @RequestParam int cantidad) {
//         carritoService.agregarProducto(usuarioId, productoId, cantidad);
//     }

//     @DeleteMapping("/eliminar")
//     public void eliminarProducto(@RequestParam Long usuarioId, @RequestParam Long productoId) {
//         carritoService.eliminarProducto(usuarioId, productoId);
//     }

//     @DeleteMapping("/vaciar/{usuarioId}")
//     public void vaciarCarrito(@PathVariable Long usuarioId) {
//         carritoService.vaciarCarrito(usuarioId);
//     }
// }

package com.analistas.luzclaritaweb.web.controller;

import java.util.Collections; // Asegúrate de importar Collections
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus; // Importar HttpStatus
import org.springframework.http.ResponseEntity; // Importar ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal; // Importar AuthenticationPrincipal
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.analistas.luzclaritaweb.model.domain.Carrito;
import com.analistas.luzclaritaweb.model.domain.Usuario; // Asegúrate de importar Usuario
import com.analistas.luzclaritaweb.model.service.ICarritoService;

@RestController
@RequestMapping("/api/carrito")
@CrossOrigin("*")
public class CarritoController {

    @Autowired
    private ICarritoService carritoService;

    // Modificado para usar AuthenticationPrincipal
    @GetMapping // Ya no necesita {usuarioId} en la URL si es para el usuario autenticado
    public ResponseEntity<List<Carrito>> obtenerCarrito(@AuthenticationPrincipal Usuario usuarioAutenticado) {
        if (usuarioAutenticado == null) {
            // Usuario no logueado, devuelve carrito vacío o un error si prefieres.
            // Devolver una lista vacía es amigable para el frontend.
            return ResponseEntity.ok(Collections.emptyList());
        }
        List<Carrito> carrito = carritoService.obtenerCarritoPorUsuario(usuarioAutenticado.getId());
        return ResponseEntity.ok(carrito);
    }

    // Modificado para usar AuthenticationPrincipal
    @PostMapping("/agregar")
    public ResponseEntity<Void> agregarProducto(@AuthenticationPrincipal Usuario usuarioAutenticado, 
                                             @RequestParam Long productoId, 
                                             @RequestParam int cantidad) {
        if (usuarioAutenticado == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // O FORBIDDEN si tiene más sentido
        }
        carritoService.agregarProducto(usuarioAutenticado.getId(), productoId, cantidad);
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
}
