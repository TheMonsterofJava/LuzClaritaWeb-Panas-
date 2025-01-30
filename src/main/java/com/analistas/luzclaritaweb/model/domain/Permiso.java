package com.analistas.luzclaritaweb.model.domain;

import lombok.Data;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

@Data
@Entity
@Table(name = "permisos")
public class Permiso {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "El nombre del permiso no puede estar vacío.")
    @Size(max = 20, message = "El nombre del permiso no debe exceder los 20 caracteres.")
    @Column(nullable = false, unique = true)
    private String nombre;

    @Size(max = 255, message = "La descripción no debe exceder los 255 caracteres.")
    private String descripcion;

    @Override
    public String toString() {
        return "Permiso{id=" + id + ", nombre='" + nombre + "', descripcion='" + descripcion + "'}";
    }
}