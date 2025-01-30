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
import lombok.Data;
import org.springframework.format.annotation.NumberFormat;

/**
 *
 * @author osval
 */
@Data
@Entity
@Table(name = "cursos")
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_curso")
    @NotEmpty(message = "El Nombre del Curso es requerido...")
    @Size(min = 5, max = 60, message = "El Nombre del Curso debe tener entre 5 y 60 caracteres...")
    private String nombre_curso;

    @Column(name = "descripcion")
    @NotEmpty(message = "La Descripcion es requerido...")
    @Size(max = 500, message = "La Descripcion debe tener un máximo de 500 caracteres...")
    private String descripcion;

    @Column(name = "precio")
    @NotEmpty(message = "El Precio es requerido...")
    @NumberFormat(pattern = "#,##0.00", style = NumberFormat.Style.CURRENCY)
    private BigDecimal precio;

    @Column(name = "link_video")
    @NotEmpty(message = "El Link del Video es requerido...")
    @Size(max = 500, message = "Su Link debe tener máximo 500 caracteres...")
    private String link_video;
   
    @NotNull(message = "La Receta es requerida")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_receta", referencedColumnName = "id")
    private Receta receta;

}
