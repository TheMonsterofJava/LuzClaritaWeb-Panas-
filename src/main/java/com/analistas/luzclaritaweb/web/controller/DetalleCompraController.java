package com.analistas.luzclaritaweb.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.analistas.luzclaritaweb.model.domain.DetalleCompra;
import com.analistas.luzclaritaweb.model.service.IDetalleCompraService;

@RestController
@RequestMapping("/detalles-compra")
@CrossOrigin(origins = "*")
public class DetalleCompraController {

    @Autowired
    private final IDetalleCompraService detalleCompraService;

    public DetalleCompraController(IDetalleCompraService detalleCompraService) {
        this.detalleCompraService = detalleCompraService;
    }

    // Listar todos los detalles de compra
    @GetMapping
    public ResponseEntity<List<DetalleCompra>> listarDetallesCompra() {
        return ResponseEntity.ok(detalleCompraService.listarDetallesCompra());
    }

    // Obtener detalle por ID
    @GetMapping("/{id}")
    public ResponseEntity<DetalleCompra> obtenerDetallePorId(@PathVariable Long id) {
        return detalleCompraService.obtenerDetallePorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Obtener detalles de una compra específica
    @GetMapping("/compra/{compraId}")
    public ResponseEntity<List<DetalleCompra>> obtenerDetallesPorCompra(@PathVariable Long compraId) {
        return ResponseEntity.ok(detalleCompraService.obtenerDetallesPorCompra(compraId));
    }

    // Crear un nuevo detalle de compra
    @PostMapping
    public ResponseEntity<DetalleCompra> crearDetalleCompra(@RequestBody DetalleCompra detalleCompra) {
        return ResponseEntity.status(HttpStatus.CREATED).body(detalleCompraService.guardarDetalleCompra(detalleCompra));
    }

    // Eliminar detalle por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarDetalleCompra(@PathVariable Long id) {
        if (detalleCompraService.obtenerDetallePorId(id).isPresent()) {
            detalleCompraService.eliminarDetalleCompra(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

}
