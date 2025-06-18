package com.analistas.luzclaritaweb.model.domain;

import java.math.BigDecimal;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

// DetalleCompra.java
@Data
@Entity
@Table(name = "detalle_compra")
@ToString(exclude = { "producto", "compra" }) // Excluir relaciones
public class DetalleCompra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La cantidad es requerida")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private int cantidad;

    @NotNull(message = "El precio unitario es requerido")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    private BigDecimal precioUnitario;

    //Saque el nullable porque no es necesario que un detalle de compra tenga un producto asociado
    // Si un detalle de compra no tiene un producto, significa que es un ingrediente directo del inventario
    // Si es un producto, entonces se asocia a un producto específico
    // Si es un ingrediente, se asocia a un ingrediente del inventario
    
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compra_id",nullable= false)
    private Compra compra;

    @ManyToOne
    @JoinColumn(name = "ingrediente_id", nullable = false)
    private Inventario ingrediente; // <--- Clave: Usar Inventario en lugar de Producto

    public BigDecimal getSubtotal() {
        return precioUnitario.multiply(new BigDecimal(cantidad));
    }
}