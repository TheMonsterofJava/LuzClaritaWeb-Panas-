package com.analistas.luzclaritaweb.model.domain;

import lombok.Data;

@Data
public class Carrito {
    private Producto producto;
    private int cantidad;

    public Carrito(Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
    }
}
