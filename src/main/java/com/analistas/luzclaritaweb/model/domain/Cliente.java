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
import lombok.Data;

@Data
@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "nomb_ape")
    @NotEmpty(message = "El Nombre y Apellido es requerido...")
    @Size(min = 5, max = 60, message = "El Nombre y Apellido debe tener entre 5 y 60 caracteres...")
    private String nomb_ape;
    
    @Column(name = "celular")
    @NotEmpty(message = "Su telefono o celular es requerido...")
    @Size(min = 14, max = 15, message = "Su telefono o celular, debe tener los 15 caracteres...")
    private Long celular;
    
    @Column(name = "correo")
    @Size(max = 120, message = "Su Email no debe pasar los 120 caracteres...")
    @NotEmpty(message = "Su Email es requerido...")
    private String correo;
    
    @Column(name = "direccion")
    @NotEmpty(message = "Su mensaje es requerido...")
    @Size(max = 120, message = "Su direccion no debe pasar los 120 caracteres...")
    private String direccion;
    
    @Column(name = "contraseña")
    @NotEmpty(message = "Su mensaje es requerido...")
    @Size(min = 10, max = 20, message = "Su contraseña debe contener entre 10 a 20 caracteres...")
    private String contraseña;
    
    @NotNull(message = "El Cliente es requerido...")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", referencedColumnName = "id")
    private Usuario usuario;

    @Override
    public String toString() {
        return id + " - " + nomb_ape;
    }
}
