/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.analistas.luzclaritaweb.model.domain;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name = "caja")
@ToString(exclude = {"usuario", "movimientos", "facturas", "compras"}) // Excluir relaciones
public class Caja {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "fecha")
    private LocalDateTime fecha;
    
    @Column(name = "saldo_inicial", nullable = false)
    private double saldoInicial;
    
    @Column(name = "saldo_final", nullable = false)
    private double saldoFinal;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoCaja estado;

    //Agregamos la columna activa con valor por defecto 
    //Para indicar si la caja está activa o no
    //Para su posterior borrado lógico
    @Column(name = "activa", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE") 
    private boolean activa = true; 
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;
    
    @OneToMany(mappedBy = "caja", fetch = FetchType.LAZY)
    private List<MovimientoCaja> movimientos;
    
    @OneToMany(mappedBy = "caja", fetch = FetchType.LAZY)
    private List<Factura> facturas;
    
    @OneToMany(mappedBy = "caja", fetch = FetchType.LAZY)
    private List<Compra> compras;
    
    public enum EstadoCaja {
        ABIERTA, CERRADA
    }
    
    @PrePersist
    public void prePersist() {
        this.fecha = LocalDateTime.now();
        this.estado = EstadoCaja.ABIERTA;
    }
}
