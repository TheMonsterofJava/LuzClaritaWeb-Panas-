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
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 *
 * @author osval
 */
@Data
@Entity
@Table(name = "proveedores")
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre")
    @Size(max = 100, message = "El Nombre del Proveedor no debe tener máximo 100 caracteres...")
    @NotEmpty(message = "El Nombre del Proveedor es requerido...")
    private String nombre;

    @Column(name = "contacto")
    @Size(max = 100, message = "El Contacto del Proveedor no debe tener máximo 100 caracteres...")
    @NotEmpty(message = "El Contacto del Proveedor es requerido...")
    private String contacto;

    @Column(name = "telefono")
    @Size(max = 50, message = "El Telefono del Proveedor no debe tener máximo 500 caracteres...")
    @NotEmpty(message = "Su nombre y Apellido es requerido...")
    private int telefono;

    @Column(name = "direccion")
    @Size(max = 250, message = "La Direccion del Proveedor no debe pasar los 120 caracteres...")
    @NotEmpty(message = "La Direccion del Proveedor es requerida...")
    private String direccion;

    @Column(name = "email")
    @Size(max = 120, message = "El Email del Proveedor no debe pasar los 120 caracteres...")
    @NotEmpty(message = "El Email del Proveedor es requerido...")
    private String email;

    //Compra
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_compra", referencedColumnName = "id")
    private Compra compra;
    
}
