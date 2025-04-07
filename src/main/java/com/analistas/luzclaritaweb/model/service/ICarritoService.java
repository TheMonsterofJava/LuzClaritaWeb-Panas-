package com.analistas.luzclaritaweb.model.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.analistas.luzclaritaweb.model.domain.Carrito;
import com.analistas.luzclaritaweb.model.domain.Producto;
import com.analistas.luzclaritaweb.model.domain.Usuario;
import com.analistas.luzclaritaweb.model.repository.ICarritoRepository;

@Service
public class ICarritoService {

    @Autowired
    private ICarritoRepository carritoRepository;

    public List<Carrito> obtenerCarritoPorUsuario(Long usuarioId) {
        return carritoRepository.findByUsuarioId(usuarioId);
    }

    public void agregarProducto(Long usuarioId, Long productoId, int cantidad) {
        Optional<Carrito> carritoExistente = carritoRepository.findByUsuarioIdAndProductoId(usuarioId, productoId);

        if (carritoExistente.isPresent()) {
            // Si ya existe, sumamos la cantidad
            Carrito item = carritoExistente.get();
            item.setCantidad(item.getCantidad() + cantidad);
            carritoRepository.save(item);
        } else {
            // Si no existe, lo creamos
            Carrito item = new Carrito();
            item.setUsuario(new Usuario());
            item.getUsuario().setId(usuarioId); // Establecer ID de usuario
            item.setProducto(new Producto());
            item.getProducto().setId(productoId); // Establecer ID de producto
            item.setCantidad(cantidad);
            carritoRepository.save(item);
        }
    }

    public void eliminarProducto(Long usuarioId, Long productoId) {
        Optional<Carrito> carritoExistente = carritoRepository.findByUsuarioIdAndProductoId(usuarioId, productoId);

        carritoExistente.ifPresent(carritoRepository::delete);
    }

    public void vaciarCarrito(Long usuarioId) {
        List<Carrito> carrito = carritoRepository.findByUsuarioId(usuarioId);
        carritoRepository.deleteAll(carrito);
    }
}
