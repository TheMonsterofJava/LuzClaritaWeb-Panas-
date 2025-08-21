package com.analistas.luzclaritaweb.model.domain;

import java.util.Date;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

//Usuario
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

    @Column(name = "clave")
    @Size(min = 5, max = 71, message = "La clave debe tener entre 5 y 71 caracteres.")
    private String clave; // Ahora es opcional, pero con validación condicional

    private String foto;
    
    private Date fecha_creacion;

    @Column(name = "activo", columnDefinition = "boolean default 1")
    private boolean activo;

    // @ManyToOne(fetch = FetchType.EAGER)
    // @JoinColumn(name = "id_permiso", referencedColumnName = "id")
    // private Permiso permiso;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_permiso", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_USUARIO_PERMISO"))
    private Permiso permiso;

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Cliente cliente;

    // Método helper para manejar la relación bidireccional
    public void setCliente(Cliente cliente) {
        if (cliente == null) {
            if (this.cliente != null) {
                this.cliente.setUsuario(null);
            }
        } else {
            cliente.setUsuario(this);
        }
        this.cliente = cliente;
    }

    @PrePersist
    public void PrePersist() {
        activo = true;
    }

    @Override
    public String toString() {
        return nomb_usu + " - " + id;
    }

}