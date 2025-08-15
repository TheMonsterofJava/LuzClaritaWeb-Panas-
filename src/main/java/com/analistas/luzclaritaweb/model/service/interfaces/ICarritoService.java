package com.analistas.luzclaritaweb.model.service.interfaces;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.analistas.luzclaritaweb.dto.CarritoDTO;
import com.analistas.luzclaritaweb.model.domain.Carrito;
import com.analistas.luzclaritaweb.model.domain.Producto;
import com.analistas.luzclaritaweb.model.domain.Receta;
import com.analistas.luzclaritaweb.model.domain.Usuario;
import com.analistas.luzclaritaweb.model.repository.ICarritoRepository;
import com.analistas.luzclaritaweb.web.controller.CarritoController.ItemCarritoLocal;
import com.analistas.luzclaritaweb.web.excepciones.CarritoSyncException;

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
    public void eliminarReceta(Long usuarioId, Long recetaId) {
        carritoRepository.deleteByUsuarioIdAndRecetaId(usuarioId, recetaId);
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

    // Actualizar la cantidad del carrito
    // Ver si funciona cuando un usuario que no esta logueado intenta actualizar la
    // cantidad de un producto en el carrito
    @Transactional
    public void actualizarCantidad(Usuario usuario, Long productoId, int cantidad) {
        Optional<Carrito> carritoOpt = carritoRepository.findByUsuarioIdAndProductoId(usuario.getId(), productoId);

        if (carritoOpt.isPresent()) {
            Carrito item = carritoOpt.get();
            item.setCantidad(cantidad);
            carritoRepository.save(item);
        }
        // Si no existe, no hacemos nada. El frontend no debería permitir llegar a este
        // caso.
    }

    @Transactional
    public void sincronizarDesdeLocalStorage(Usuario usuario, List<ItemCarritoLocal> itemsLocal) {
        try {
            for (ItemCarritoLocal item : itemsLocal) {
                try {
                    if ("receta".equals(item.getTipo())) {
                        agregarReceta(usuario, item.getId(), item.getCantidad());
                    } else {
                        agregarProducto(usuario, item.getId(), item.getCantidad());
                    }
                } catch (CarritoSyncException e) {
                    // Si el item ya existe, simplemente lo ignoramos y continuamos con el
                    // siguiente.
                    System.out.println("Item ya sincronizado, ignorando: " + e.getMessage());
                }
            }
        } catch (DataAccessException e) {
            // Captura otros errores de base de datos que no sean duplicados.
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

    @Transactional
    public void agregarReceta(Usuario usuario, Long recetaId, int cantidad) {
        try {
            Optional<Carrito> carritoExistente = carritoRepository.findByUsuarioIdAndRecetaId(usuario.getId(),
                    recetaId);

            if (carritoExistente.isPresent()) {
                // This is the fix: prevent duplicates instead of increasing quantity.
                throw new CarritoSyncException("La receta ya se encuentra en el carrito.");
            } else {
                // If it doesn't exist, create it with quantity 1.
                Carrito item = new Carrito();
                item.setUsuario(usuario);
                Receta recetaRef = new Receta();
                recetaRef.setId(recetaId);
                item.setReceta(recetaRef);
                item.setCantidad(1); // Recipes should always have a quantity of 1.
                carritoRepository.save(item);
            }

        } catch (DataAccessException e) {
            throw new CarritoSyncException("Error de base de datos al agregar la receta al carrito.", e);
        }
    }
}