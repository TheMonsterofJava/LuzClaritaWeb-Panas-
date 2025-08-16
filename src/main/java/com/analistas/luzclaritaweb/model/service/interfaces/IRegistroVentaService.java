package com.analistas.luzclaritaweb.model.service.interfaces;

import java.math.BigDecimal;
import java.util.List;
import com.analistas.luzclaritaweb.model.domain.RegistroVenta;

public interface IRegistroVentaService {
    
    /**
     * Crea y guarda un registro de una venta.
     * @param descripcion El nombre o descripción del ítem vendido.
     * @param cantidad La cantidad de unidades vendidas.
     * @param precioUnitario El precio de una sola unidad.
     */
    void registrarVenta(String descripcion, int cantidad, BigDecimal precioUnitario);

    /**
     * Busca todos los registros de ventas.
     * @return Una lista con todos los registros de venta.
     */
    List<RegistroVenta> buscarTodos();

}