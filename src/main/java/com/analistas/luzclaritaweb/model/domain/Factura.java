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
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
//import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;


@Data
@Entity
@Table(name = "facturas")
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_factura")
    @NotEmpty(message = "Su Numero de Factura es requerido...")
    private Integer numero_factura;

    @Column(name = "fecha_pedido")
    @NotEmpty(message = "La Fecha es requerida...")
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

    public Double calcularTotal() {
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
