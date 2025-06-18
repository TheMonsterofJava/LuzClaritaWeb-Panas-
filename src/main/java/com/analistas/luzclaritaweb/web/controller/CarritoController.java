package com.analistas.luzclaritaweb.web.controller;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.analistas.luzclaritaweb.model.domain.Carrito;
import com.analistas.luzclaritaweb.model.service.ICarritoService;

@RestController
@RequestMapping("/api/carrito")
@CrossOrigin("*")  // Permitir peticiones desde frontend
public class CarritoController {

    @Autowired
    private ICarritoService carritoService;

    @GetMapping("/{usuarioId}")
    public List<Carrito> obtenerCarrito(@PathVariable Long usuarioId) {
        return carritoService.obtenerCarritoPorUsuario(usuarioId);
    }

    @PostMapping("/agregar")
    public void agregarProducto(@RequestParam Long usuarioId, @RequestParam Long productoId, @RequestParam int cantidad) {
        carritoService.agregarProducto(usuarioId, productoId, cantidad);
    }

    @DeleteMapping("/eliminar")
    public void eliminarProducto(@RequestParam Long usuarioId, @RequestParam Long productoId) {
        carritoService.eliminarProducto(usuarioId, productoId);
    }

    @DeleteMapping("/vaciar/{usuarioId}")
    public void vaciarCarrito(@PathVariable Long usuarioId) {
        carritoService.vaciarCarrito(usuarioId);
    }
}
