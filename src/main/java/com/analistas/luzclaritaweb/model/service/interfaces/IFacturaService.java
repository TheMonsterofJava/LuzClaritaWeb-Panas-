package  com.analistas.luzclaritaweb.model.service.interfaces;

import java.util.List;

import com.analistas.luzclaritaweb.dto.CarritoDTO;
import com.analistas.luzclaritaweb.model.domain.Factura;
import com.analistas.luzclaritaweb.model.domain.Usuario;

public interface IFacturaService {
    
    Factura guardar(Factura factura);
    
    Factura buscarPorId(Long id);
    
    List<Factura> buscarTodas();
    
    List<Factura> buscarPorUsuario(Usuario usuario);
    
    Factura crearFacturaDesdeCarrito(List<CarritoDTO> itemsCarrito, Usuario usuario, String metodoPago);
    
    String generarNumeroFactura();
    
    void actualizarInventario(List<CarritoDTO> itemsCarrito);

    List<Factura> buscarPorIdUsuario(Long id);
    
}