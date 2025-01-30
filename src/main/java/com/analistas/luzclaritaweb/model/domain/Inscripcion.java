/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.analistas.luzclaritaweb.model.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
//import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import jakarta.persistence.Table;
import java.util.Date;
import lombok.Data;

/**
 *
 * @author osval
 */
@Data
@Entity
@Table(name = "inscripciones")
public class Inscripcion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    //Faltan Poner datos
    @Column(name = "fecha_inscripcion")
    private Date fecha_inscripcion;
    
   
   @ManyToOne
   @JoinColumn(name = "id_cliente", referencedColumnName = "id")
   private Cliente cliente;
    
  
   @ManyToOne
   @JoinColumn(name = "id_curso", referencedColumnName = "id")
   private Curso curso;
}
