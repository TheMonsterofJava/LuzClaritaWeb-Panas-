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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 *
 * @author osval
 */
@Data
@Entity
@Table(name = "consultas")
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom_ape")
    @Size(max = 50, message = "Su nombre y apellido no debe tener máximo 50 caracteres...")
    @NotEmpty(message = "Su nombre y Apellido es requerido...")
    private String nom_ape;

    @Column(name = "correo")
    @Size(max = 120, message = "Su Email no debe tener máximo 120 caracteres...")
    @NotEmpty(message = "Su Email es requerido...")
    private String correo;

    @Column(name = "celular")
    @Size(max = 15, message = "Su Celular no debe tener máximo 15 caracteres...")
    @NotEmpty(message = "Su Celular es requerido...")
    @Pattern(regexp = "^\\+?\\d{1,4}?[-.\\s]?\\(?\\d{1,3}?\\)?[-.\\s]?\\d{1,4}[-.\\s]?\\d{1,4}[-.\\s]?\\d{1,9}$")
    private String celular;

    @Column(name = "mensaje")
    @Size(max = 500, message = "Su mensaje no debe tener máximo 500 caracteres...")
    @NotEmpty(message = "Su mensaje es requerido...")
    private String mensaje;

    @Column(name = "reseña")
    @Size(max = 500)
    private String reseña;

    @Column(name = "activo", columnDefinition = "boolean default 0")
    private boolean activo;
    
    @PrePersist
    public void PrePersist() {
        activo = false;
    }
}
