package com.analistas.luzclaritaweb.model.domain;

import java.math.BigDecimal;

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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;
@Data
@Entity
@Table(name = "detalle_venta")
public class DetalleVenta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_venta", nullable = false)
    private Venta venta;
    
    @Column(name = "item_id", nullable = false)
    private Long itemId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_item", nullable = false)
    private TipoItem tipoItem;
    
    @Column(name = "cantidad", nullable = false)
    private int cantidad;
    
    @Column(name = "precio_unitario", nullable = false)
    @NumberFormat(pattern = "#,##0.00")
    private BigDecimal precioUnitario;
    
    @Column(name = "subtotal", nullable = false)
    @NumberFormat(pattern = "#,##0.00")
    private BigDecimal subtotal;
    
    @PrePersist
    @PreUpdate
    public void calcularSubtotal() {
        this.subtotal = precioUnitario.multiply(new BigDecimal(cantidad));
    }
    
    public enum TipoItem {
        PRODUCTO, CURSO
    }
}