package com.analistas.luzclaritaweb.model.service;

import com.analistas.luzclaritaweb.model.domain.Carrito;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.ArrayList;

@Service
public class CarritoServiceImpl implements ICarritoService {

    private final List<Carrito> productos = new ArrayList<>();

    @Override
    public void agregarProducto(Carrito carrito) {
        productos.add(carrito);
    }

    @Override
    public List<Carrito> obtenerProductos() {
        return productos;
    }

    @Override
    public void vaciarCarrito() {
        productos.clear();
    }
}