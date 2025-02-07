package com.analistas.luzclaritaweb.web.controller;

import com.analistas.luzclaritaweb.model.domain.Carrito;
import com.analistas.luzclaritaweb.model.domain.Producto;
import com.analistas.luzclaritaweb.model.service.ICarritoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/carrito")
public class CarritoController {

    @Autowired
    private ICarritoService carritoService;

    @PostMapping("/agregar")
    public String agregarProducto(@RequestParam("productoId") Long productoId, @RequestParam("cantidad") int cantidad) {
        Producto producto = new Producto(); // Obtener el producto por ID (esto es solo un ejemplo)
        producto.setId(productoId);
        Carrito carrito = new Carrito(producto, cantidad);
        carritoService.agregarProducto(carrito);
        return "redirect:/carrito/ver";
    }
@GetMapping("/ver")
public String verCarrito(Model model) {
    List<Carrito> cartItems = carritoService.obtenerProductos();
    model.addAttribute("cartItems", cartItems);

    // Usamos BigDecimal para calcular el total
    BigDecimal total = cartItems.stream()
        .map(item -> item.getProducto().getPrecio().multiply(BigDecimal.valueOf(item.getCantidad())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    model.addAttribute("total", total);
    return "carrito";
}


    @PostMapping("/comprar")
    public String comprar() {
        carritoService.vaciarCarrito();
        return "redirect:/carrito/ver";
    }
}
