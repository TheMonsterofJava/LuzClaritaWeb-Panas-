package com.analistas.luzclaritaweb.model.domain;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotEmpty;
//import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Agregamos un email para poder despues verificarlo con el ... y para posterior
    // registro con una API de google
    @Column(name = "email", nullable = false, unique = true, length = 100) // este email es unico es por ello que se
                                                                           // puede usar junto con el ID para verficar
                                                                           // si el usuario no esta repetido para mas
                                                                           // seguridad
    private String email;

    @NotEmpty(message = "El Nombre de Usuario es requerido...")
    @Size(min = 5, max = 30, message = "El Nombre de Usuario debe tener entre 5 y 30 caracteres...")
    private String nomb_usu;

    @NotEmpty(message = "La clave es requerida...")
    @Size(min = 5, max = 71/* limitado por el bycrypt */, message = "La clave debe tener al menos 5 caracteres...")
    private String clave;

    private Date fecha_creacion;

    @Column(name = "activo", columnDefinition = "boolean default 1")
    private boolean activo;

    @ManyToOne(fetch = FetchType.EAGER) 
    @JoinColumn(name = "id_permiso", referencedColumnName = "id") 
    private Permiso permiso;

    @PrePersist
    public void PrePersist() {
        activo = true;
    }

    @Override
    public String toString() {
        return nomb_usu + " - " + id;
    }

}



