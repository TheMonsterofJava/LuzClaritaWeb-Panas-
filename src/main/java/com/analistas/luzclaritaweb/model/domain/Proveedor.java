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
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 *
 * @author osval
 */

//Proveedor
@Data
@Entity
@Table(name = "proveedores")
public class Proveedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 100, message = "El Nombre del Proveedor no debe tener máximo 100 caracteres...")
    @NotEmpty(message = "El Nombre del Proveedor es requerido...")
    private String nombre;

    @Size(max = 100, message = "El Contacto del Proveedor no debe tener máximo 100 caracteres...")
    @NotEmpty(message = "El Contacto del Proveedor es requerido...")
    private String contacto;

    @Size(max = 50, message = "El Telefono del Proveedor no debe tener más de 50 caracteres...")
    @NotEmpty(message = "El Telefono del Proveedor es requerido...")
    private String telefono;

    @Size(max = 255, message = "La Direccion del Proveedor no debe pasar los 255 caracteres...")
    @NotEmpty(message = "La Direccion del Proveedor es requerida...")
    private String direccion;

    @Size(max = 120, message = "El Email del Proveedor no debe pasar los 120 caracteres...")
    @NotEmpty(message = "El Email del Proveedor es requerido...")
    private String email;

    @Column(name = "activo", columnDefinition = "boolean default true")
    private boolean activo = true;

    @PrePersist
    public void prePersist() {
        activo = true;
    }
}