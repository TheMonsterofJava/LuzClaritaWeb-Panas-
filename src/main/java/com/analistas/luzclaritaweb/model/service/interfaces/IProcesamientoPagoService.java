package com.analistas.luzclaritaweb.model.service.interfaces;

import com.analistas.luzclaritaweb.dto.CarritoDTO;
import com.analistas.luzclaritaweb.model.domain.Factura;
import com.analistas.luzclaritaweb.model.domain.Usuario;
import com.analistas.luzclaritaweb.web.excepciones.MontoDelPagoNoCoincideException;
import com.analistas.luzclaritaweb.web.excepciones.OrdenYaProcesadaException;
import com.analistas.luzclaritaweb.web.excepciones.PagoNoAprobadoException;
import com.analistas.luzclaritaweb.web.excepciones.StockInsuficienteException;

import java.math.BigDecimal;
import java.util.List;

public interface IProcesamientoPagoService {

    Factura procesarPagoExitosoMP(
        List<CarritoDTO> itemsCarrito,
        Usuario usuario,
        String metodoPago,
        String collectionId,    // ID de pago de Mercado Pago
        String externalReference, // Nuestra referencia externa (UUID)
        BigDecimal montoPagadoEnMP, // Monto que Mercado Pago reporta como pagado
        String estadoPagoMP     // Estado del pago reportado por MP (ej. "approved")
    ) throws MontoDelPagoNoCoincideException, OrdenYaProcesadaException, StockInsuficienteException, PagoNoAprobadoException, Exception;
}
