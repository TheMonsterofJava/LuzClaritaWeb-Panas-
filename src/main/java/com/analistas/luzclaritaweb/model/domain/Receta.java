/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.analistas.luzclaritaweb.model.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 *
 * @author osval
 */


@Data
@Entity
@Table(name = "recetas")
public class Receta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_receta")
    @NotEmpty(message = "El Nombre de la Receta es requerida...")
    @Size(min = 10, max = 100, message = "El Nombre del la Receta debe tener hasta 100 caracteres...")
    private String nombre_receta;

    @Column(name = "descripcion")
    @NotEmpty(message = "La descripcion es requerida...")
    @Size(max = 500, message = "La Descripcion debe tener hasta 500 caracteres...")
    private String descripcion;

    @Column(name = "ingredientes")
    @NotEmpty(message = "El ingrediente es requerido...")
    @Size(max =250, message = "Los Ingredientes deben tener hasta 250 caracteres...")
    private String ingredientes;

    @Column(name = "pasos")
    @NotEmpty(message = "Los Pasos de la Receta son requeridos...")
    @Size(max =500, message = "Los Pasos de la Rectea deben tener hasta 500 caracteres...")
    private String pasos;

    @Column(name = "tiempo")
    @NotEmpty(message = "El Tiempo de duracion de la receta es requerido...")
    @Size(max =50, message = "El Tiempo de duracion de la receta debe tener hasta 50 caracteres...")
    private String tiempo;

    @Column(name = "imagen_link")
    @NotEmpty(message = "El Link de la Imagen es requerido...")
    @Size(max =500, message = "El Link de la Imagen debe tener hasta 500 caracteres...")
    private String imagen_link;

   
}
