package com.analistas.luzclaritaweb.model.service;

import com.analistas.luzclaritaweb.model.domain.Carrito;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public interface ICarritoService {
    void agregarProducto(Carrito carrito);
    List<Carrito> obtenerProductos();
    void vaciarCarrito();
}