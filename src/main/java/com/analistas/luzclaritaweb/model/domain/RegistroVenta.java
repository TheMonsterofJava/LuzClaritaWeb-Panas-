package com.analistas.luzclaritaweb.model.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Data
@Table(name = "registros_ventas")
public class RegistroVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private LocalDateTime fecha;

    @NotEmpty
    @Column(name = "descripcion_producto")
    private String descripcionProducto;

    @NotNull
    private int cantidad;

    @NotNull
    @Column(name = "precio_unitario", precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @NotNull
    @Column(name = "total_venta", precision = 10, scale = 2)
    private BigDecimal totalVenta;
}
