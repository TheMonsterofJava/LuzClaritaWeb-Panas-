package com.analistas.luzclaritaweb.web.controller;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.analistas.luzclaritaweb.dto.CarritoDTO;
import com.analistas.luzclaritaweb.model.domain.DetalleVenta;
import com.analistas.luzclaritaweb.model.domain.Producto;
import com.analistas.luzclaritaweb.model.domain.Usuario;
import com.analistas.luzclaritaweb.model.domain.Venta;
import com.analistas.luzclaritaweb.model.repository.IProductoRepository;
import com.analistas.luzclaritaweb.model.service.interfaces.ICarritoService;
import com.analistas.luzclaritaweb.model.service.interfaces.IVentaService;
import com.analistas.luzclaritaweb.web.config.security.CustomUserDetails;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercadopago.resources.Preference;
import com.mercadopago.resources.datastructures.preference.BackUrls;
import com.mercadopago.resources.datastructures.preference.Item;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import com.mercadopago.resources.Payment;
import com.mercadopago.exceptions.MPException;
import java.util.Map;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class MercadoPagoController {

    @Value("${app.base-url}")
    private String baseUrl;

    @Autowired
    private IProductoRepository productoRepository;

    @Autowired
    private ICarritoService carritoService;

    @Autowired
    private IVentaService ventaService;


    @PostMapping("/createAndRedirect")
    public String createAndRedirect(@RequestParam("cartData") String cartDataJson,
            Model model,
            Authentication authentication,
            RedirectAttributes flash,
            HttpSession session) {

        log.info("--- INICIANDO PROCESO DE PAGO CON MERCADOPAGO ---");
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("Intento de pago sin autenticación.");
                flash.addFlashAttribute("error", "Usuario no autenticado.");
                return "redirect:/inicioSesion/login";
            }

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Usuario usuario = userDetails.getUsuario();
            log.info("Usuario autenticado: {}", usuario.getNomb_usu());

            ObjectMapper objectMapper = new ObjectMapper();
            List<CarritoDTO> carritoItems = objectMapper.readValue(cartDataJson, new TypeReference<List<CarritoDTO>>() {});
            log.info("Carrito JSON recibido y parseado. Items: {}", carritoItems.stream().map(Object::toString).collect(Collectors.joining(", ")));


            if (carritoItems.isEmpty()) {
                log.warn("El carrito está vacío. Abortando pago.");
                flash.addFlashAttribute("error", "El carrito está vacío");
                return "redirect:/home";
            }

            log.info("Validando stock de productos...");
            for (CarritoDTO item : carritoItems) {
                Producto producto = productoRepository.findById(item.getProductoId()).orElse(null);
                if (producto == null) {
                    log.error("Producto no encontrado con ID: {}. Abortando.", item.getProductoId());
                    flash.addFlashAttribute("error", "Producto no encontrado: " + item.getProductoId());
                    return "redirect:/home";
                }
                if (producto.getStock() < item.getCantidad()) {
                    log.error("Stock insuficiente para '{}'. Requerido: {}, Disponible: {}. Abortando.", producto.getDescripcion(), item.getCantidad(), producto.getStock());
                    flash.addFlashAttribute("error", "No hay suficiente stock para " + producto.getDescripcion());
                    return "redirect:/home";
                }
            }
            log.info("Validación de stock completada exitosamente.");

            // --- INICIO: Lógica de Venta Pendiente ---
            Venta ventaPendiente = new Venta();
            ventaPendiente.setCliente(usuario.getCliente());
            ventaPendiente.setVendedor(usuario);
            ventaPendiente.setMetodoPago("MercadoPago");
            ventaPendiente.setEstado(Venta.EstadoVenta.PENDIENTE); // Estado inicial

            List<DetalleVenta> detallesVenta = new ArrayList<>();
            java.math.BigDecimal total = java.math.BigDecimal.ZERO;
            for (CarritoDTO item : carritoItems) {
                Producto producto = productoRepository.findById(item.getProductoId()).get();
                DetalleVenta detalleVenta = new DetalleVenta();
                detalleVenta.setVenta(ventaPendiente);
                detalleVenta.setItemId(producto.getId());
                detalleVenta.setTipoItem(DetalleVenta.TipoItem.PRODUCTO);
                detalleVenta.setCantidad(item.getCantidad());
                detalleVenta.setPrecioUnitario(producto.getPrecio());
                // Calcular el subtotal manualmente aquí para evitar el NullPointerException
                BigDecimal subtotal = producto.getPrecio().multiply(new BigDecimal(item.getCantidad()));
                detalleVenta.setSubtotal(subtotal); // Asignarlo explícitamente
                detallesVenta.add(detalleVenta);
                total = total.add(subtotal);
            }
            ventaPendiente.setDetalles(detallesVenta);
            ventaPendiente.setTotal(total);
            
            // Guardar la venta pendiente para obtener su ID
            ventaService.guardarVenta(ventaPendiente);
            log.info("Venta PENDIENTE guardada con ID: {}", ventaPendiente.getId());
            // --- FIN: Lógica de Venta Pendiente ---

            Preference preference = new Preference();
            preference.setExternalReference(String.valueOf(ventaPendiente.getId())); // Usar el ID de la venta como referencia
            preference.setNotificationUrl(baseUrl + "/api/mercadopago/notificaciones");

            preference.setBackUrls(new BackUrls()
                    .setFailure(baseUrl + "/failure")
                    .setPending(baseUrl + "/pending")
                    .setSuccess(baseUrl + "/success?venta_id=" + ventaPendiente.getId())); // Pasar el ID a la página de éxito
            log.info("Back URLs configuradas: success={}, failure={}, pending={}", preference.getBackUrls().getSuccess(), preference.getBackUrls().getFailure(), preference.getBackUrls().getPending());
            log.info("Notification URL configurada: {}", preference.getNotificationUrl());


            for (CarritoDTO item : carritoItems) {
                Producto producto = productoRepository.findById(item.getProductoId()).get();
                Item mpItem = new Item();
                mpItem.setTitle(producto.getDescripcion())
                      .setQuantity(item.getCantidad())
                      .setUnitPrice(producto.getPrecio().floatValue());
                preference.appendItem(mpItem);
            }
            log.info("Items añadidos a la preferencia de MercadoPago.");

            var result = preference.save();
            log.info("Preferencia de MercadoPago creada con ID: {}. Redirigiendo a: {}", result.getId(), result.getInitPoint());
            return "redirect:" + result.getInitPoint();

        } catch (Exception e) {
            log.error("Error catastrófico en /createAndRedirect", e);
            flash.addFlashAttribute("error", "Error al procesar la solicitud de pago: " + e.getMessage());
            return "redirect:/home";
        }
    }

    @GetMapping("/success")
    public String success(@RequestParam("venta_id") Long ventaId, Model model, RedirectAttributes flash, Authentication authentication) {

        log.info("--- REDIRECCIÓN A /success INVOCADA PARA VENTA ID: {} ---", ventaId);

        if (authentication == null || !authentication.isAuthenticated()) {
            flash.addFlashAttribute("warning", "Tu sesión ha expirado, pero no te preocupes. Si tu pago fue exitoso, lo hemos registrado. Revisa tu perfil de compras.");
            return "redirect:/home";
        }
        
        Venta venta = ventaService.buscarPorId(ventaId);
        if (venta == null) {
            flash.addFlashAttribute("error", "No se encontró la venta correspondiente a tu compra.");
            return "redirect:/home";
        }
        
        // Aquí se asume que el webhook ya procesó el pago.
        // La página de éxito es solo para la experiencia del usuario.
        model.addAttribute("venta", venta);
        model.addAttribute("titulo", "¡Gracias por tu compra!");
        log.info("Mostrando página de éxito para la venta ID: {}.", ventaId);

        // Limpiar el carrito de la base de datos después de una compra exitosa.
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Usuario usuario = userDetails.getUsuario();
        carritoService.vaciarCarrito(usuario.getId());
        log.info("Carrito vaciado para el usuario ID: {}", usuario.getId());

        return "success";
    }

    @GetMapping("/failure")
    public String failure(RedirectAttributes flash) {
        log.warn("Redirigiendo al usuario a HOME desde /failure.");
        flash.addFlashAttribute("error", "El pago fue rechazado o cancelado. Por favor, intenta de nuevo.");
        return "redirect:/home";
    }

    @GetMapping("/pending")
    public String pending(RedirectAttributes flash) {
        log.info("Redirigiendo al usuario a HOME desde /pending.");
        flash.addFlashAttribute("info", "El pago está pendiente de procesamiento. Te notificaremos cuando se apruebe.");
        return "redirect:/home";
    }

    @PostMapping("/api/mercadopago/notificaciones")
    @ResponseBody
    public ResponseEntity<String> recibirNotificaciones(@RequestBody Map<String, Object> notification) {
        log.info("--- NOTIFICACIÓN DE MERCADOPAGO RECIBIDA ---");
        log.info("Cuerpo de la notificación: {}", notification);

        String type = (String) notification.get("type");
        String action = (String) notification.get("action");

        if ("payment".equals(type) || "payment.created".equals(action) || "payment.updated".equals(action)) {
            String paymentId = "";
            if (notification.containsKey("data")) {
                Map<String, Object> data = (Map<String, Object>) notification.get("data");
                paymentId = (String) data.get("id");
            } else if (notification.containsKey("resource")) {
                // Fallback for other notification formats
                String resourceUrl = (String) notification.get("resource");
                paymentId = resourceUrl.substring(resourceUrl.lastIndexOf("/") + 1);
            }

            if (paymentId == null || paymentId.isEmpty()) {
                log.warn("No se pudo extraer el ID de pago de la notificación: {}", notification);
                return ResponseEntity.badRequest().body("ID de pago no encontrado en la notificación.");
            }
            
            log.info("Acción de actualización de pago. ID de pago: {}", paymentId);

            try {
                Payment payment = Payment.findById(paymentId);
                if (payment == null) {
                    log.warn("Pago no encontrado en MercadoPago con ID: {}", paymentId);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Pago no encontrado");
                }

                log.info("Estado del pago: {}", payment.getStatus());
                log.info("Referencia externa: {}", payment.getExternalReference());

                if ("approved".equals(payment.getStatus().toString())) {
                    Long ventaId = Long.parseLong(payment.getExternalReference());
                    ventaService.procesarVentaExitosa(ventaId);
                    log.info("Pago APROBADO para venta ID: {}. Venta procesada exitosamente.", ventaId);
                } else {
                    log.info("El estado del pago es '{}', no se procesará la venta.", payment.getStatus());
                }

            } catch (MPException e) {
                log.error("Error al consultar la API de MercadoPago", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar la notificación");
            } catch (Exception e) {
                log.error("Error inesperado al manejar la notificación", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno");
            }
        }

        return ResponseEntity.ok("Notificación recibida");
    }
}