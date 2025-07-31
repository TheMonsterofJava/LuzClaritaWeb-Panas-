package com.analistas.luzclaritaweb.model.service.interfaces;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.analistas.luzclaritaweb.model.domain.Compra;

public interface ICompraService {

    //Listar Compras
    List<Compra> listarCompras();

    //Buscar Compra por ID
    Optional<Compra> obtenerCompraPorId(Long id);

    //Guardar Compra
    Compra guardarCompra(Compra compra);

    //Eliminar Compra
    void eliminarCompra(Long id);

    // Métodos personalizados
    List<Compra> buscarPorRangoDeFechas(LocalDateTime inicio, LocalDateTime fin);
    
    //Buscar compras por usuario, proveedor y caja
    List<Compra> buscarPorUsuario(Long usuarioId);
    List<Compra> buscarPorProveedor(Long proveedorId);
    List<Compra> buscarPorCaja(Long cajaId);

    // Método para obtener compras activas
    List<Compra> listarComprasActivas();

    //Buscar por ID 
    Optional<Compra> buscarPorId(Long id);

    Compra buscarPorIdConDetalles(Long id);
    
}