package com.analistas.luzclaritaweb.model.domain;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class Carrito {

    private final List<Producto> productos = new ArrayList<>();

    public void agregarProducto(Producto producto) {
        productos.add(producto);
    }

    public void quitarProducto(Long id) {
        productos.removeIf(producto -> producto.getId().equals(id));
    }

    public void vaciar() {
        productos.clear();
    }

    public List<Producto> getProductos() {
        return productos;
    }
}
