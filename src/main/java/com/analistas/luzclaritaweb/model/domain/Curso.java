package com.analistas.luzclaritaweb.model.domain;

import java.math.BigDecimal;
import java.util.List;

import org.hibernate.validator.constraints.URL;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Entity
@Table(name = "cursos")
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_curso")
    @NotEmpty(message = "El Nombre del Curso es requerido...")
    @Size(min = 5, max = 60, message = "El Nombre del Curso debe tener entre 5 y 60 caracteres...")
    private String nombreCurso;

    @Column(name = "descripcion")
    @NotEmpty(message = "La Descripcion es requerida...")
    @Size(max = 500, message = "La Descripcion debe tener un máximo de 500 caracteres...")
    private String descripcion;

    @Column(name = "precio")
    @NotNull(message = "El Precio es requerido...")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0...")
    private BigDecimal precio;

    @Column(name = "video_url")
    @URL(message = "Debe ser una URL válida")
    @Pattern(regexp = "^(https?://)?(www\\.)?(youtube\\.com|youtu\\.?be)/.+$", 
    message = "Debe ser un enlace de YouTube válido")
    private String videoUrl;

    @Column(name = "imagen_url", length = 500)
    @URL(message = "Debe ser una URL válida para la imagen")
    private String imagenUrl;

    @ElementCollection
    @CollectionTable(name = "curso_ingredientes", joinColumns = @JoinColumn(name = "curso_id"))
    @Column(name = "ingrediente")
    private List<String> ingredientes; // Ej: ["200g harina", "3 huevos"]

    @Column(name = "pasos", columnDefinition = "TEXT", nullable = false)
    private String pasos; // Pasos detallados (1. Mezclar..., 2. Hornear...)

    @Column(name = "consejos", columnDefinition = "TEXT")
    private String consejos; // Tips adicionales

    private boolean requiereCompra; // true = de pago, false = gratuito
}