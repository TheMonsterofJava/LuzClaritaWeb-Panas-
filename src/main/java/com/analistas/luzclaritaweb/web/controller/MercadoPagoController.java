package com.analistas.luzclaritaweb.web.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.analistas.luzclaritaweb.dto.CarritoDTO;
import com.analistas.luzclaritaweb.model.domain.Factura;
import com.analistas.luzclaritaweb.model.domain.Producto;
import com.analistas.luzclaritaweb.model.domain.RegistroVenta;
import com.analistas.luzclaritaweb.model.domain.Usuario;
import com.analistas.luzclaritaweb.model.repository.IRegistroVentaRepository;
import com.analistas.luzclaritaweb.model.repository.IUsuarioRepository;
import com.analistas.luzclaritaweb.model.repository.IFacturaRepository;
import com.analistas.luzclaritaweb.model.service.interfaces.ICarritoService;
import com.analistas.luzclaritaweb.model.service.interfaces.IFacturaService;
import com.analistas.luzclaritaweb.model.service.interfaces.IProductoService;
import com.analistas.luzclaritaweb.web.config.security.CustomUserDetails;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.Preference;
import com.mercadopago.resources.datastructures.preference.BackUrls;
import com.mercadopago.resources.datastructures.preference.Item;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class MercadoPagoController {

    @Value("${app.base-url}")
    private String baseUrl;

    @Autowired
    private IProductoService productoService;

    @Autowired
    private IFacturaService facturaService;

    @Autowired
    private ICarritoService carritoService;

    @Autowired
    private IRegistroVentaRepository registroVentaRepository;

    @Autowired
    private IUsuarioRepository usuarioRepository;

    @Autowired
    private IFacturaRepository facturaRepository;

    @PostMapping("/createAndRedirect")
    @SuppressWarnings("CallToPrintStackTrace")
    public String createAndRedirect(@RequestParam("cartData") String cartDataJson,
            Model model,
            Authentication authentication) throws MPException {

        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                model.addAttribute("error", "Usuario no autenticado.");
                return "redirect:/inicioSesion/login";
            }

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Usuario usuario = userDetails.getUsuario();

            if (usuario == null) {
                model.addAttribute("error", "No se pudieron cargar los datos del usuario.");
                return "redirect:/inicioSesion/login";
            }

            ObjectMapper objectMapper = new ObjectMapper();
            List<CarritoDTO> carritoItems = objectMapper.readValue(cartDataJson,
                    new TypeReference<List<CarritoDTO>>() {
                    });

            if (carritoItems.isEmpty()) {
                model.addAttribute("error", "El carrito está vacío");
                return "redirect:/home?error=carrito_vacio";
            }

            // Validar stock ANTES de crear la preferencia
            for (CarritoDTO item : carritoItems) {
                Producto producto = productoService.buscarPorId(item.getProductoId());
                if (producto == null) {
                    model.addAttribute("error", "Producto no encontrado: " + item.getProductoId());
                    return "redirect:/home?error=producto_no_encontrado";
                }
                if (producto.getStock() < item.getCantidad()) {
                    model.addAttribute("error", "No hay suficiente stock para " + producto.getDescripcion());
                    return "redirect:/home?error=sin_stock";
                }
            }

            // Verificar si ya existe una factura pendiente para evitar duplicados
            String externalReference = usuario.getId().toString() + "_" + System.currentTimeMillis();

            Preference preference = new Preference();
            preference.setBackUrls(new BackUrls()
                    .setFailure(baseUrl + "/failure")
                    .setPending(baseUrl + "/pending")
                    .setSuccess(baseUrl + "/success"));
            
            preference.setExternalReference(externalReference);

            // Crear los items de MercadoPago
            for (CarritoDTO item : carritoItems) {
                Producto producto = productoService.buscarPorId(item.getProductoId());
                if (producto != null) {
                    Item mpItem = new Item();
                    mpItem.setTitle(producto.getDescripcion())
                            .setQuantity(item.getCantidad())
                            .setUnitPrice(producto.getPrecio().floatValue());

                    preference.appendItem(mpItem);
                }
            }

            var result = preference.save();
            System.out.println("MercadoPago preference created. Redirect URL: " + result.getInitPoint());
            System.out.println("External Reference: " + externalReference);

            return "redirect:" + result.getInitPoint();

        } catch (MPException e) {
            System.err.println("Error de MercadoPago: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error al procesar el pago con MercadoPago.");
            return "redirect:/home?error=error_mercadopago";
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            System.err.println("Error al procesar JSON del carrito: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error al procesar los datos del carrito.");
            return "redirect:/home?error=error_datos";
        } catch (IllegalArgumentException e) {
            System.err.println("Error de validación: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error en los datos proporcionados.");
            return "redirect:/home?error=error_validacion";
        }
    }

    @GetMapping("/success")
    public String success(HttpServletRequest request,
            @RequestParam("collection_id") String collectionId,
            @RequestParam("collection_status") String collectionStatus,
            @RequestParam("external_reference") String externalReference,
            @RequestParam("payment_type") String paymentType,
            @RequestParam("merchant_order_id") String merchantOrderId,
            @RequestParam("preference_id") String preferenceId,
            @RequestParam("site_id") String siteId,
            @RequestParam("processing_mode") String processingMode,
            @RequestParam("merchant_account_id") String merchantAccountId,
            Model model) throws MPException {

        System.out.println("--- PAGO RECIBIDO, PROCESANDO /success ---");
        System.out.println("Collection ID: " + collectionId);
        System.out.println("Collection Status: " + collectionStatus);
        System.out.println("External Reference: " + externalReference);
        System.out.println("Preference ID: " + preferenceId);

        if (!"approved".equals(collectionStatus)) {
            System.out.println("El pago no fue aprobado, redirigiendo a /failure");
            return "redirect:/failure";
        }

        // Verificar si esta factura ya fue procesada para evitar duplicados
        if (facturaRepository.existsByMpCollectionId(collectionId)) {
            System.out.println("Esta transacción ya fue procesada anteriormente");
            model.addAttribute("titulo", "Transacción ya procesada");
            model.addAttribute("mensaje", "Esta compra ya fue registrada en el sistema.");
            return "ventas/success";
        }

        try {
            // Obtener información del pago desde MercadoPago
            var payment = com.mercadopago.resources.Payment.findById(collectionId);
            var preference = Preference.findById(preferenceId);
            
            // Extraer el usuario ID del external_reference
            String userIdStr = externalReference.split("_")[0];
            Long userId = Long.parseLong(userIdStr);
            
            Usuario usuario = usuarioRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + userId));

            // AQUÍ ESTÁ LA CLAVE: Obtener los datos desde la preferencia de MercadoPago
            // en lugar de desde el carrito (que puede estar vacío)
            List<CarritoDTO> itemsCarrito = new ArrayList<>();
            
            for (Item mpItem : preference.getItems()) {
                // Buscar el producto por nombre (ya que MP solo devuelve el título)
                Producto producto = productoService.buscarPorNombre(mpItem.getTitle());
                if (producto != null) {
                    CarritoDTO carritoDTO = new CarritoDTO();
                    carritoDTO.setProductoId(producto.getId());
                    carritoDTO.setProductoNombre(producto.getDescripcion());
                    carritoDTO.setProductoPrecio(producto.getPrecio());
                    carritoDTO.setCantidad(mpItem.getQuantity());
                    itemsCarrito.add(carritoDTO);
                }
            }

            if (itemsCarrito.isEmpty()) {
                System.err.println("No se pudieron recuperar los items del carrito desde MercadoPago");
                model.addAttribute("error", "Error al procesar los items de la compra");
                return "ventas/failure";
            }

            // Verificar stock nuevamente antes de procesar
            for (CarritoDTO item : itemsCarrito) {
                Producto producto = productoService.buscarPorId(item.getProductoId());
                if (producto == null || producto.getStock() < item.getCantidad()) {
                    model.addAttribute("error", "Stock insuficiente para completar la compra");
                    return "ventas/failure";
                }
            }

            // Crear la factura usando tu servicio existente
            Factura factura = facturaService.crearFacturaDesdeCarrito(itemsCarrito, usuario, "MercadoPago");
            
            // Agregar información de MercadoPago a la factura
            factura.setMpCollectionId(collectionId);
            factura.setMpExternalReference(externalReference);
            factura = facturaRepository.save(factura);

            // Procesar cada item: descontar stock y crear registro de venta
            for (CarritoDTO item : itemsCarrito) {
                Producto producto = productoService.buscarPorId(item.getProductoId());
                if (producto != null) {
                    // Descontar stock
                    int nuevoStock = producto.getStock() - item.getCantidad();
                    producto.setStock(nuevoStock);
                    productoService.guardar(producto);

                    // Crear registro de venta
                    RegistroVenta registro = new RegistroVenta();
                    registro.setFecha(LocalDateTime.now());
                    registro.setDescripcionProducto(producto.getDescripcion());
                    registro.setCantidad(item.getCantidad());
                    registro.setPrecioUnitario(producto.getPrecio());
                    registro.setTotalVenta(producto.getPrecio().multiply(new java.math.BigDecimal(item.getCantidad())));
                    registroVentaRepository.save(registro);

                    System.out.println("Stock actualizado para " + producto.getDescripcion() + 
                                     ": " + (producto.getStock() + item.getCantidad()) + " -> " + producto.getStock());
                }
            }

            // Vaciar el carrito del usuario
            carritoService.vaciarCarrito(usuario.getId());

            System.out.println("✅ Venta procesada exitosamente. Factura ID: " + factura.getId());

            model.addAttribute("payment", payment);
            model.addAttribute("factura", factura);
            model.addAttribute("titulo", "¡Compra Exitosa!");

            return "ventas/success";

        } catch (Exception e) {
            System.err.println("Error al procesar la venta: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error interno al procesar la venta");
            return "ventas/failure";
        }
    }

    @GetMapping("/failure")
    public String failure(HttpServletRequest request,
            @RequestParam("collection_id") String collectionId,
            @RequestParam("collection_status") String collectionStatus,
            @RequestParam("external_reference") String externalReference,
            @RequestParam("payment_type") String paymentType,
            @RequestParam("merchant_order_id") String merchantOrderId,
            @RequestParam("preference_id") String preferenceId,
            @RequestParam("site_id") String siteId,
            @RequestParam("processing_mode") String processingMode,
            @RequestParam("merchant_account_id") String merchantAccountId,
            Model model) {

        System.out.println("--- PAGO RECHAZADO ---");
        System.out.println("Collection ID: " + collectionId);
        System.out.println("Collection Status: " + collectionStatus);

        model.addAttribute("titulo", "Pago Rechazado");
        model.addAttribute("error", "El pago no pudo ser procesado. Por favor, intente nuevamente.");

        return "ventas/failure";
    }

    @GetMapping("/pending")
    public String pending(HttpServletRequest request,
            @RequestParam("collection_id") String collectionId,
            @RequestParam("collection_status") String collectionStatus,
            @RequestParam("external_reference") String externalReference,
            @RequestParam("payment_type") String paymentType,
            @RequestParam("merchant_order_id") String merchantOrderId,
            @RequestParam("preference_id") String preferenceId,
            @RequestParam("site_id") String siteId,
            @RequestParam("processing_mode") String processingMode,
            @RequestParam("merchant_account_id") String merchantAccountId,
            Model model) {

        System.out.println("--- PAGO PENDIENTE ---");
        System.out.println("Collection ID: " + collectionId);

        model.addAttribute("titulo", "Pago Pendiente");
        model.addAttribute("mensaje", "Su pago está siendo procesado. Le notificaremos cuando sea aprobado.");

        return "ventas/pending";
    }
}