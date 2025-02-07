/*package com.analistas.luzclaritaweb.web.controller;

import com.mercadopago.resources.preference.Preference;
import com.mercadopago.resources.preference.Item;
import com.mercadopago.exceptions.MPException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/mercadopago")
public class MercadoPagoController {

    @Autowired
    private MP mercadoPago;  // Instancia del SDK configurada en MercadoPagoConfig

    @PostMapping("/create-payment")
    public Map<String, String> createPayment(@RequestBody List<Producto> cart) {
        try {
            // Crear la preferencia de pago
            Preference preference = new Preference();
            
            // Agregar los productos del carrito
            List<Item> items = cart.stream().map(producto -> {
                Item item = new Item();
                item.setTitle(producto.getDescripcion());
                item.setQuantity(1); // Suponiendo que la cantidad es 1, puedes adaptarlo según tu lógica
                item.setUnitPrice(Double.valueOf(producto.getPrecio()));
                return item;
            }).collect(Collectors.toList());

            preference.setItems(items);

            // Establecer los parámetros de pago
            preference.setBackUrls(Map.of(
                "success", "http://localhost:8080/payment-success",
                "failure", "http://localhost:8080/payment-failure",
                "pending", "http://localhost:8080/payment-pending"
            ));
            preference.setAutoReturn("approved");

            // Guardar la preferencia
            preference.save();

            // Devolver el URL para redirigir al usuario a Mercado Pago
            return Map.of("init_point", preference.getInitPoint());
        } catch (MPException e) {
            e.printStackTrace();
            return Map.of("error", "Error al generar el pago.");
        }
    }
}
*/