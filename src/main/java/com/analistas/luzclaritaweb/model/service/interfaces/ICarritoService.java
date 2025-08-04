package com.analistas.luzclaritaweb.model.service.interfaces;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.analistas.luzclaritaweb.dto.CarritoDTO;
import com.analistas.luzclaritaweb.model.domain.Carrito;
import com.analistas.luzclaritaweb.model.domain.Producto;
import com.analistas.luzclaritaweb.model.domain.Usuario;
import com.analistas.luzclaritaweb.model.repository.ICarritoRepository;


@Service
public class ICarritoService {

    @Autowired
    private ICarritoRepository carritoRepository;

    @Transactional(readOnly = true)
    public List<CarritoDTO> obtenerCarritoPorUsuario(Long usuarioId) {
        List<Carrito> carritoList = carritoRepository.findByUsuarioId(usuarioId);
        return carritoList.stream()
                .map(CarritoDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void agregarProducto(Usuario usuario, Long productoId, int cantidad) {
        Optional<Carrito> carritoExistente = carritoRepository.findByUsuarioIdAndProductoId(usuario.getId(), productoId);

        if (carritoExistente.isPresent()) {
            // Si ya existe, sumamos la cantidad
            Carrito item = carritoExistente.get();
            item.setCantidad(item.getCantidad() + cantidad);
            carritoRepository.save(item);
        } else {
            // Si no existe, lo creamos
            Carrito item = new Carrito();
            item.setUsuario(usuario); // Usar la entidad de usuario gestionada
            Producto productoRef = new Producto();
            productoRef.setId(productoId);
            item.setProducto(productoRef); // Establecer la referencia del producto
            item.setCantidad(cantidad);
            carritoRepository.save(item);
        }
    }

    @Transactional
    public void sincronizarDesdeLocalStorage(Usuario usuario, List<com.analistas.luzclaritaweb.web.controller.CarritoController.ItemCarritoLocal> itemsLocal) {
        for (com.analistas.luzclaritaweb.web.controller.CarritoController.ItemCarritoLocal item : itemsLocal) {
            // Reutilizamos la lógica de agregarProducto que ya maneja si el item existe o no.
            agregarProducto(usuario, item.getId(), item.getCantidad());
        }
    }

    @Transactional
    public void eliminarProducto(Long usuarioId, Long productoId) {
        Optional<Carrito> carritoExistente = carritoRepository.findByUsuarioIdAndProductoId(usuarioId, productoId);

        carritoExistente.ifPresent(carritoRepository::delete);
    }

    public void vaciarCarrito(Long usuarioId) {
        List<Carrito> carrito = carritoRepository.findByUsuarioId(usuarioId);
        carritoRepository.deleteAll(carrito);
    }
}