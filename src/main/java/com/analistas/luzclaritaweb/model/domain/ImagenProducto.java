package com.analistas.luzclaritaweb.model.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "imagenes_producto")
public class ImagenProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreArchivo;
    private String rutaArchivo;
    private String tipoMime;
    private boolean principal; // Para marcar imagen principal

    private long tamanio;

    public void setTamanio(long tamanio) {
        this.tamanio = tamanio;
    }

    public long getTamanio() {
        return tamanio;
    }

    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Producto producto;
}