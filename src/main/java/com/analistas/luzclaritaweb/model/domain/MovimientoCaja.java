package com.analistas.luzclaritaweb.model.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.format.annotation.NumberFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;;

@Data
@Entity
@Table(name = "movimientos_caja")
public class MovimientoCaja {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;
    
    @Column(name = "monto", nullable = false)
    @NumberFormat(pattern = "#,##0.00")
    private BigDecimal monto;

     // Se elimina montoDouble para usar solo BigDecimal monto para precisión.
    // private double montoDouble; 

    @Column(name = "tipo", nullable = false, length = 10)
    private String tipo; // "INGRESO" o "EGRESO"
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_operacion", nullable = false)
    private TipoOperacion tipoOperacion;
    
    @Column(name = "descripcion", length = 255)
    private String descripcion;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_caja", referencedColumnName = "id")
    private Caja caja;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", referencedColumnName = "id")
    private Usuario operador;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_factura", referencedColumnName = "id")
    private Factura factura;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_venta", referencedColumnName = "id")
    private Venta venta;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_compra", referencedColumnName = "id")
    private Compra compra;
    
    @PrePersist
    public void prePersist() {
        this.fecha = LocalDateTime.now();
    }
    
    public enum TipoOperacion {
        INGRESO, EGRESO
    }
}