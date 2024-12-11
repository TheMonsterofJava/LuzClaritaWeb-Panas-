/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;
import org.springframework.format.annotation.NumberFormat;

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

    @Column(name = "nombre_ingrediente")
    @NotEmpty(message = "El Nombre del Ingrediente es requerido...")
    @Size(max = 50, message = "El Nombre del Ingrediente no debe pasar los 50 caracteres...")
    private String nombre_ingrediente;

    @Column(name = "precio")
    @NotNull(message = "El precio es requerido...")
    @NumberFormat(pattern = "#,##0.00", style = NumberFormat.Style.CURRENCY)
    private BigDecimal precio;

    @Column(name = "cantidad")
    @NotEmpty(message = "La Cantidad del Ingrediente es requerido...")
    @Size(max = 100)
    private int cantidad;

    @Column(name = "unidad_medida")
    @NotEmpty(message = "La Unidad de Medida del Ingrediente es requerida...")
    @Size(max = 100)
    private String unidad_medida;

    @Column(name = "fecha_ingreso")
    @NotEmpty(message = "La Fecha de Adquisision del Ingrediente es requerida...")
    private Date fecha_ingreso;

    @Column(name = "fecha_vencimiento")
    @NotEmpty(message = "La Fecha de Vencimiento del Ingrediente es requerida...")
    private Date fecha_vencimiento;

    @NotNull(message = "El Proveedor es requerido")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proveedor", referencedColumnName = "id")
    private Proveedor proveedor;
   
     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "id_usuario", referencedColumnName = "id")
     private Usuario usuario;
    
}
