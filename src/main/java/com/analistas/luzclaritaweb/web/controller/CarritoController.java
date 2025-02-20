package com.analistas.luzclaritaweb.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.analistas.luzclaritaweb.model.domain.Carrito;
import com.analistas.luzclaritaweb.model.domain.Producto;

import java.util.List;

@RestController
@RequestMapping("/carrito")
public class CarritoController {

    @Autowired
    private Carrito carrito;

    @PostMapping("/agregar")
    public void agregarProducto(@RequestBody Producto producto) {
        carrito.agregarProducto(producto);
    }

    @DeleteMapping("/quitar/{id}")
    public void quitarProducto(@PathVariable Long id) {
        carrito.quitarProducto(id);
    }

    @DeleteMapping("/vaciar")
    public void vaciarCarrito() {
        carrito.vaciar();
    }

    @GetMapping("/productos")
    public List<Producto> obtenerProductos() {
        return carrito.getProductos();
    }
}