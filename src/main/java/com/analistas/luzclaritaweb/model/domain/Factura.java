package com.analistas.luzclaritaweb.model.domain;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;


@Data
@Entity
//Vamos a Añadir Indices para Busquedas Comunes en la tabla
//Los indixes sirven para mejorar el rendimiento de las consultas en la base de datos.
//En este caso, se añaden índices para los campos mpCollectionId y mpExternalReference
//Esto es especialmente útil si se realizan muchas búsquedas o filtrados por estos campos.
@Table(name = "facturas", indexes = {
    @Index(name = "idx_factura_mp_collection_id", columnList = "mpCollectionId"),
    @Index(name = "idx_factura_mp_external_ref", columnList = "mpExternalReference")
})
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "numero_factura", unique = true) //El numero de factura es unico para cada compra que tienen distintos clientes.
    @NotEmpty(message = "Su Numero de Factura es requerido...")
    private String numero_factura;

    @Column(name = "fecha_pedido")
    // @NotEmpty // LocalDateTime no puede ser @NotEmpty, se valida con @NotNull o se asigna siempre
    // @NotEmpty(message = "La Fecha es requerida...")
    private LocalDateTime fecha_pedido;

    //Estado del Pedido
    @Column(name = "activo", columnDefinition = "boolean default 1")
    private boolean activo;

    @Column(name = "metodo_pago")
    @NotEmpty(message = "El Metodo de Pago es requerido...")
    private String metodo_pago;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", referencedColumnName = "id")
    private Cliente cliente;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_caja", referencedColumnName = "id")
    private Caja caja;
    
    @OneToMany(mappedBy = "factura", fetch = FetchType.LAZY)
    private List<Detalle_factura> detalles;

    //Nuevos Campos para Mercado Pago
    @Column(name = "mp_collection_id", length = 100)
    private String mpCollectionId;

    @Column(name = "mp_external_reference", length = 100)
    private String mpExternalReference;

    public Double calcularTotal() {
        if (detalles == null) return 0.0;
        return detalles.stream()
                .mapToDouble(Detalle_factura::calcularSubtotal)
                .sum();
    }
}

//CODIGO ANTERIOR

//@Data
//@Entity
//@Table(name = "facturas")
//public class Factura {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(name = "numero_factura")
//
//    @NotEmpty(message = "Su Numero de Factura es requerido...")
//    @Size(max = 300, message = "El numero de factura debe tener hasta 300 caracteres...")
//    private Long numero_factura;
//
//    @Column(name = "fecha_pedido")
//    @NotEmpty(message = "La Fecha es requerida...")
//    private LocalDateTime fecha_pedido;
//
//    //Estado del Pedido
//    @Column(name = "activo", columnDefinition = "boolean default 1")
//    private boolean activo;
//
//    @Column(name = "metodo_pago")
//    @NotEmpty(message = "El Metodo de Pago es requerido...")
//    private String metodo_pago;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "id_cliente", referencedColumnName = "id")
//    private Cliente cliente;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "id_detalle_factura", referencedColumnName = "id")
//    private Detalle_factura detalle_factura;
//
//    private List<Detalle_factura> detalles;
//    //private Cajas cajas;
//
//    public Double calcularTotal() {
//
//        Double total = 0.0;
//
//        for (Detalle_factura detalle : detalles) {
//            //Obtener el subtotal y acumularlo...
//            total += detalle.calcularSubtotal();
//        }
//
//        return total;
//    }
//
//    public void AddDetalles(Detalle_factura detalle) {
//        detalles.add(detalle);
//    }
//}
