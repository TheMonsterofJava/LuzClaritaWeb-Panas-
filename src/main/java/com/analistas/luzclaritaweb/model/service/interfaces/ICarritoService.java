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
import com.analistas.luzclaritaweb.web.controller.CarritoController.ItemCarritoLocal;
import com.analistas.luzclaritaweb.web.excepciones.CarritoSyncException;
import org.springframework.dao.DataAccessException;

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
        try {

            Optional<Carrito> carritoExistente = carritoRepository.findByUsuarioIdAndProductoId(usuario.getId(),
                    productoId);

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

        } catch (DataAccessException e) {
            // Envolver la excepción de base de datos en una excepción personalizada
            throw new CarritoSyncException("Error de base de datos al agregar el producto al carrito.", e);
        
        }

    }

    @Transactional
    public void sincronizarDesdeLocalStorage(Usuario usuario, List<ItemCarritoLocal> itemsLocal) {
        try {
            for (ItemCarritoLocal item : itemsLocal) {
                agregarProducto(usuario, item.getId(), item.getCantidad());
            }
        } catch (DataAccessException e) {
            // Envolver la excepción de base de datos en una excepción personalizada
            throw new CarritoSyncException("Error de base de datos durante la sincronización del carrito.", e);
        }
    }

    @Transactional
    public void eliminarProducto(Long usuarioId, Long productoId) {
        Optional<Carrito> carritoExistente = carritoRepository.findByUsuarioIdAndProductoId(usuarioId, productoId);

        carritoExistente.ifPresent(carritoRepository::delete);
    }

    @Transactional
    public void vaciarCarrito(Long usuarioId) {
        List<Carrito> carrito = carritoRepository.findByUsuarioId(usuarioId);
        carritoRepository.deleteAll(carrito);
    }
}