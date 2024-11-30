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
import jakarta.validation.constraints.NotNull;
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

    @NotEmpty(message = "El nombre es requerido...")
    @Size(min = 5, max = 20, message = "El nombre debe tener entre 5 y 30 caracteres...")
    private String nombre;

    @NotEmpty(message = "La clave es requerida...")
    @Size(min = 6, max = 70, message = "La clave debe tener al menos 6 caracteres...")
    private String clave;

    private Date fecha_creacion;

    @Column(name = "activo", columnDefinition = "boolean default 1")
    private boolean activo;

    @NotNull(message = "El permiso es requerido")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_permiso", referencedColumnName = "id")
    private Permiso permiso;

     @NotNull(message = "El Inventario es requerido")
     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "id_inventario", referencedColumnName = "id")
     private Inventario inventario;
    
    @PrePersist
    public void PrePersist() {
        activo = true;
    }

    @Override
    public String toString() {
        return nombre + " - " + id;
    }

    
}
