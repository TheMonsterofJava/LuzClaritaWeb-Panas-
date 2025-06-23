package com.analistas.luzclaritaweb.model.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

import lombok.Data;
import org.hibernate.validator.constraints.URL;

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

    @Column(name = "porciones")
    @Min(value = 1, message = "Debe tener al menos 1 porción")
    private Integer porciones;

    @Column(name = "tiempo_preparacion")
    @Min(value = 1, message = "El tiempo mínimo es 1 minuto")
    private Integer tiempoPreparacion; // en minutos

    @Column(name = "dificultad")
    @NotNull(message = "La dificultad es requerida")
    private String dificultad; // Baja, Media, Alta

    @ElementCollection
    @CollectionTable(name = "curso_ingredientes", joinColumns = @JoinColumn(name = "curso_id"))
    @Column(name = "ingrediente")
    private List<String> ingredientes;

    @Column(name = "pasos", columnDefinition = "TEXT", nullable = false)
    private String pasos;

    @Column(name = "consejos", columnDefinition = "TEXT")
    private String consejos;

    private boolean requiereCompra;
}