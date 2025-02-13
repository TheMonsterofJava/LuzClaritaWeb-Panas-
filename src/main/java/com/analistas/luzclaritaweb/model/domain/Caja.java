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
import java.time.LocalDateTime;

import lombok.Data;
import org.springframework.format.annotation.NumberFormat;

/**
 *
 * @author osval
 */
@Data
@Entity
@Table(name = "caja")
public class Caja {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "fecha")
    private LocalDateTime fecha;
    
    @Column(name = "monto_total")
    @NumberFormat(pattern = "#,##0.00", style = NumberFormat.Style.CURRENCY)
    private double monto_total;
    
   
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", referencedColumnName = "id")
    private Usuario usuario;
}

