package com.analistas.luzclaritaweb.dto;

//Clase DTO para el carrito de compras

import java.math.BigDecimal;

import com.analistas.luzclaritaweb.model.domain.Carrito;

// Esta clase puede ser utilizada para transferir datos del carrito entre la capa de servicio y la
// capa de presentación, o para serializar/deserializar datos en formato JSON.
// Agregar lombok @Data para generar getters, setters, toString, equals y hashCode automáticamente

public class CarritoDTO {

    private int cantidad;
    private Long productoId;
    private String productoNombre;
    private BigDecimal productoPrecio;
    private String productoLinkImagen;

    //constructor vacio
    //No borrar
    //Sirve para deserializar el JSON de cartData
    public CarritoDTO() {
    }

    public CarritoDTO(Carrito carrito) {
        this.cantidad = carrito.getCantidad();
        this.productoId = carrito.getProducto().getId();
        this.productoNombre = carrito.getProducto().getDescripcion();
        this.productoPrecio = carrito.getProducto().getPrecio();
        this.productoLinkImagen = carrito.getProducto().getLinkImagen();
    }

    // Getters y Setters
    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public String getProductoNombre() {
        return productoNombre;
    }

    public void setProductoNombre(String productoNombre) {
        this.productoNombre = productoNombre;
    }

    public BigDecimal getProductoPrecio() {
        return productoPrecio;
    }

    public void setProductoPrecio(BigDecimal productoPrecio) {
        this.productoPrecio = productoPrecio;
    }

    public String getProductoLinkImagen() {
        return productoLinkImagen;
    }

    public void setProductoLinkImagen(String productoLinkImagen) {
        this.productoLinkImagen = productoLinkImagen;
    }
}
