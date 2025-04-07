package  com.analistas.luzclaritaweb.model.service;

import java.util.List;

import com.analistas.luzclaritaweb.model.domain.Carrito;
import com.analistas.luzclaritaweb.model.domain.Factura;
import com.analistas.luzclaritaweb.model.domain.Usuario;

public interface IFacturaService {
    
    Factura guardar(Factura factura);
    
    Factura buscarPorId(Long id);
    
    List<Factura> buscarTodas();
    
    List<Factura> buscarPorUsuario(Usuario usuario);
    
    Factura crearFacturaDesdeCarrito(List<Carrito> itemsCarrito, Usuario usuario, String metodoPago);
    
    String generarNumeroFactura();
    
    void actualizarInventario(List<Carrito> itemsCarrito);
}