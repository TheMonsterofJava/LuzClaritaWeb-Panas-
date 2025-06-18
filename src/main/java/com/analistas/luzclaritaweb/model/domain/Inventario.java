package com.analistas.luzclaritaweb.model.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;;

/**
 *
 * @author osval
 */
@Data
@Entity
@Table(name = "inventario")
public class Inventario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_Ingrediente")
    @NotEmpty(message = "El Nombre del Ingrediente es requerido...")
    @Size(max = 50, message = "El Nombre del Ingrediente no debe pasar los 50 caracteres...")
    private String nombreIngrediente;

    @Column(name = "unidad_Medida")
    @NotEmpty(message = "La Unidad de Medida del Ingrediente es requerida...")
    @Size(max = 100)
    private String unidadMedida;

    @NotNull(message = "El precio es requerido...")
    @Column(precision = 10, scale = 2)
    private BigDecimal precio;

    @NotNull(message = "La Cantidad del Ingrediente es requerido...")
    @Min(value = 1, message = "La cantidad debe ser mayor que 0")
    private int cantidad;

    @Column(name = "fecha_ingreso")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "La fecha de ingreso es obligatoria")
    private LocalDate fechaIngreso;

    @Column(name = "fecha_vencimiento")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "La fecha de vencimiento es obligatoria")
    private LocalDate fechaVencimiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proveedor", nullable = false)
    private Proveedor proveedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = true)
    private Usuario usuario;

    @Column(name = "activo", columnDefinition = "boolean default true")
    private boolean activo = true;

    @PrePersist
    public void prePersist() {
        activo = true;
    }
}
