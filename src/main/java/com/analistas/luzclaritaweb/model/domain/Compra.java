package com.analistas.luzclaritaweb.model.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.mercadopago.core.annotations.validation.Size;

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
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name = "compras")
@ToString(exclude = { "caja", "proveedor", "usuario", "detalles" }) // Excluir relaciones
public class Compra {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "fecha_hora")
        private LocalDateTime fechaHora = LocalDateTime.now();

        @NotEmpty
        @Size(max = 255)
        private String descripcion;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "id_caja", nullable = false)
        private Caja caja;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "id_proveedor", nullable = false)
        private Proveedor proveedor;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "id_usuario", nullable = false)
        private Usuario usuario;

        @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<DetalleCompra> detalles = new ArrayList<>();

        @Column(name = "activo", columnDefinition = "boolean default true")
        private boolean activo = true;

        @Column(name = "total", precision = 10, scale = 2)
        private BigDecimal total;

        // Método para calcular y establecer el total
        public void calcularYEstablecerTotal() {
                this.total = detalles == null || detalles.isEmpty() ? BigDecimal.ZERO
                                : detalles.stream()
                                                .map(DetalleCompra::getSubtotal)
                                                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
}