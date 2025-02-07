package com.analistas.luzclaritaweb.model.domain;

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
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import org.springframework.format.annotation.NumberFormat;

@Data
@Entity
@Table(name = "compras")
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fechaHora")
    private LocalDateTime fechaHora;

    @Column(name = "descripcion")
    @NotEmpty(message = "La Descripcion es requerido...")
    @Size(max = 500, message = "La Descripcion debe tener un máximo de 500 caracteres...")
    private String descripcion;

    @NotNull(message = "El precio es requerido...")
    @NumberFormat(pattern = "#,##0.00", style = NumberFormat.Style.CURRENCY)
    private BigDecimal precio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_caja", referencedColumnName = "id")
    private Caja caja;

    @OneToMany(mappedBy = "compra", fetch = FetchType.LAZY)
    private List<DetalleCompra> compras;

    @Column(name = "activo", columnDefinition = "boolean default 1")
    private boolean activo;

    public Compra() {
        compras = new ArrayList<>();
        fechaHora = LocalDateTime.now();
        descripcion = "Ninguna";
        activo = true;
    }

    public Double calcularTotal() {

        Double total = 0.0;

        for (DetalleCompra compra : compras) {
            total += compra.calcularSubtotal();
        }
        return total;
    }

    @PrePersist
    public void PrePersist() {
        activo = true;
    }
}