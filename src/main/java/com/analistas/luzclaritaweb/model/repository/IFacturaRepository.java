package com.analistas.luzclaritaweb.model.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import com.analistas.luzclaritaweb.model.domain.Factura;
import com.analistas.luzclaritaweb.model.domain.Usuario;

@Repository
public interface IFacturaRepository extends JpaRepository<Factura, Long> {
    List<Factura> findByClienteUsuario(Usuario usuario);
    
    @Query("SELECT COUNT(f) FROM Factura f WHERE f.fecha_pedido BETWEEN :inicio AND :fin")
    Long countByFechaPedidoBetween(@Param("inicio") LocalDateTime inicio, 
                                 @Param("fin") LocalDateTime fin);

    //Metodo para verificar la existencia de una factura por el ID de Colección de Mercado Pago
    boolean existsByMpCollectionId(String mpCollectionId);
    //Metodo para encontrar una factura por el ID de Colección de Mercado Pago
    Optional<Factura> findByMpExternalReference(String mpExternalReference);

    //Futros metodos
    // Opcional
    // boolean existsByMpExternalReference(String mpExternalReference);
    // Optional<Factura> findByMpExternalReference(String mpExternalReference);
}


