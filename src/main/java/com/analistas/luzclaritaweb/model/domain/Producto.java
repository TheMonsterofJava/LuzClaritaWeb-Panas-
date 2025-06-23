package com.analistas.luzclaritaweb.model.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.NumberFormat;
import org.springframework.format.annotation.NumberFormat.Style;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotEmpty(message = "El nombre es requerido...")
    @Size(max = 65, message = "El nombre debe tener máximo 65 caracteres...")
    private String nombre;

    @NotEmpty(message = "La descripción es requerida...")
    @Size(max = 260, message = "La descripción debe tener máximo 260 caracteres...")
    private String descripcion;

    @Column(columnDefinition = "decimal(10, 2) default 0.00")
    @NotNull(message = "El precio es requerido...")
    @Positive(message = "El precio debe ser mayor que cero")
    @NumberFormat(pattern = "#,##0.00", style = Style.CURRENCY)
    private BigDecimal precio;

    @NotNull(message = "El stock es requerido...")
    private Integer stock;

    @Column(name = "lnk_img", length = 1000)
    private String linkImagen;

    @Column(name = "activo", columnDefinition = "boolean default 1")
    private boolean activo;

    @NotNull(message = "La categoría es requerida")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria", referencedColumnName = "id")
    private Categoria categoria;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ImagenProducto> imagenes = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        activo = true;
    }

    // Constructor sin argumentos
    public Producto() {
    }

    public Producto(Long id, String descripcion, Double precio) {
        this.id = id;
        this.descripcion = descripcion;
        this.precio = BigDecimal.valueOf(precio);
    }

    // Métodos auxiliares para manejo de imágenes
    public void agregarImagen(ImagenProducto imagen) {
        imagenes.add(imagen);
        imagen.setProducto(this);
    }

    public void eliminarImagen(ImagenProducto imagen) {
        imagenes.remove(imagen);
        imagen.setProducto(null);
    }

    public BigDecimal getPrecio() {
        return precio;
    }
}
