package com.analistas.luzclaritaweb.model.domain;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Data
@Table(name = "carrito") 
public class Carrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El Usuario es requerido")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", referencedColumnName = "id")
    private Usuario usuario;

    @NotNull(message = "El Producto es requerido")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", referencedColumnName = "id")
    private Producto producto;

    @NotNull
    @Column(columnDefinition = "INT NOT NULL DEFAULT 1")
    private int cantidad;
}
