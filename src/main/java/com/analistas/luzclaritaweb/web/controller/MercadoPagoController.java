package com.analistas.luzclaritaweb.web.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.analistas.luzclaritaweb.dto.CarritoDTO;
import com.analistas.luzclaritaweb.model.domain.Caja;
import com.analistas.luzclaritaweb.model.domain.Detalle_factura;
import com.analistas.luzclaritaweb.model.domain.Factura;
import com.analistas.luzclaritaweb.model.domain.Producto;
import com.analistas.luzclaritaweb.model.domain.Usuario;
import com.analistas.luzclaritaweb.model.repository.IDetalleFacturaRepository;
import com.analistas.luzclaritaweb.model.repository.IFacturaRepository;
import com.analistas.luzclaritaweb.model.repository.IProductoRepository;
import com.analistas.luzclaritaweb.model.service.interfaces.ICajaService;
import com.analistas.luzclaritaweb.model.service.interfaces.ICarritoService;
import com.analistas.luzclaritaweb.web.config.security.CustomUserDetails;
import com.analistas.luzclaritaweb.web.excepciones.StockInsuficienteException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercadopago.resources.Preference;
import com.mercadopago.resources.datastructures.preference.BackUrls;
import com.mercadopago.resources.datastructures.preference.Item;

import jakarta.servlet.http.HttpServletRequest;
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
    private IFacturaRepository facturaRepository;

    @Autowired
    private IDetalleFacturaRepository detalleFacturaRepository;

    @Autowired
    private ICajaService cajaService;


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

            String externalReference = usuario.getId().toString() + "_" + System.currentTimeMillis();
            log.info("Generada External Reference para MP: {}", externalReference);

            Preference preference = new Preference();
            preference.setBackUrls(new BackUrls()
                    .setFailure(baseUrl + "/failure")
                    .setPending(baseUrl + "/pending")
                    .setSuccess(baseUrl + "/success"));
            log.info("Back URLs configuradas: success={}, failure={}, pending={}", preference.getBackUrls().getSuccess(), preference.getBackUrls().getFailure(), preference.getBackUrls().getPending());

            preference.setExternalReference(externalReference);

            for (CarritoDTO item : carritoItems) {
                Producto producto = productoRepository.findById(item.getProductoId()).get();
                Item mpItem = new Item();
                mpItem.setTitle(producto.getDescripcion())
                      .setQuantity(item.getCantidad())
                      .setUnitPrice(producto.getPrecio().floatValue());
                preference.appendItem(mpItem);
            }
            log.info("Items añadidos a la preferencia de MercadoPago.");

            log.info("Guardando carrito y usuario en la sesión HTTP...");
            session.setAttribute("carrito_mp", carritoItems);
            session.setAttribute("usuario_mp", usuario);
            log.info("Datos guardados en sesión. Clave 'carrito_mp' y 'usuario_mp'.");


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
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public String success(HttpServletRequest request,
            @RequestParam("collection_id") String collectionId,
            @RequestParam("collection_status") String collectionStatus,
            @RequestParam("external_reference") String externalReference,
            Model model,
            HttpSession session,
            RedirectAttributes flash) {

        log.info("--- CALLBACK DE MERCADOPAGO /success INVOCADO ---");
        log.info("Parámetros recibidos -> collection_id: {}, collection_status: {}, external_reference: {}", collectionId, collectionStatus, externalReference);

        if (!"approved".equals(collectionStatus)) {
            log.warn("El pago no fue aprobado. Estado: {}. Redirigiendo a /failure.", collectionStatus);
            flash.addFlashAttribute("error", "El pago no fue aprobado por MercadoPago. Estado: " + collectionStatus);
            return "redirect:/failure";
        }
        log.info("El pago fue aprobado por MercadoPago.");

        if (facturaRepository.existsByMpCollectionId(collectionId)) {
            log.warn("Intento de procesar una transacción duplicada. Collection ID: {}. Redirigiendo a home.", collectionId);
            flash.addFlashAttribute("warning", "Esta compra ya fue registrada anteriormente.");
            return "redirect:/home";
        }
        log.info("La transacción es nueva. Collection ID: {} no existe en la BD.", collectionId);
        
        log.info("Recuperando carrito y usuario de la sesión HTTP...");
        List<CarritoDTO> carritoItems = (List<CarritoDTO>) session.getAttribute("carrito_mp");
        Usuario usuario = (Usuario) session.getAttribute("usuario_mp");
        
        if (carritoItems == null || usuario == null) {
            log.error("Error crítico: No se encontraron datos del carrito o del usuario en la sesión. La sesión puede haber expirado.");
            flash.addFlashAttribute("error", "Tu sesión ha expirado. No se pudo completar la compra.");
            return "redirect:/home";
        }
        log.info("Datos de sesión recuperados exitosamente para usuario: {}. Items en carrito: {}", usuario.getNomb_usu(), carritoItems.size());

        try {
            // LÓGICA DE NEGOCIO DIRECTAMENTE EN EL CONTROLADOR
            log.info("Iniciando lógica de negocio directamente en el controlador.");

            if (usuario.getCliente() == null) {
                log.error("Error crítico: El usuario {} no tiene un cliente asociado.", usuario.getNomb_usu());
                throw new IllegalStateException("El usuario no tiene un cliente asociado");
            }

            Caja cajaActiva = cajaService.buscarUltimaCajaAbiertaYActiva(Caja.EstadoCaja.ABIERTA)
                    .orElseThrow(() -> new IllegalStateException("No hay ninguna caja activa en el sistema."));
            log.info("Caja activa encontrada: ID {}", cajaActiva.getId());

            // 1. Crear la Factura
            Factura factura = new Factura();
            Long ultimoNumero = facturaRepository.countByFechaPedidoBetween(LocalDateTime.now().toLocalDate().atStartOfDay(), LocalDateTime.now());
            factura.setNumero_factura("FAC-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" + String.format("%04d", ultimoNumero + 1));
            factura.setFecha_pedido(LocalDateTime.now());
            factura.setMetodo_pago("MercadoPago");
            factura.setCliente(usuario.getCliente());
            factura.setActivo(true);
            factura.setCaja(cajaActiva);
            factura.setMpCollectionId(collectionId);
            factura.setMpExternalReference(externalReference);
            
            Factura facturaGuardada = facturaRepository.save(factura);
            log.info("Factura guardada con ID temporal: {}", facturaGuardada.getId());

            // 2. Procesar detalles y descontar stock
            List<Detalle_factura> detalles = new ArrayList<>();
            for (CarritoDTO item : carritoItems) {
                log.info("Procesando item: Producto ID {}, Cantidad: {}", item.getProductoId(), item.getCantidad());
                Producto producto = productoRepository.findById(item.getProductoId())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + item.getProductoId()));

                log.info("Producto '{}' encontrado. Stock actual: {}", producto.getDescripcion(), producto.getStock());
                if (producto.getStock() < item.getCantidad()) {
                    log.error("Stock insuficiente para producto: '{}'. Requerido: {}, Disponible: {}", producto.getDescripcion(), item.getCantidad(), producto.getStock());
                    throw new StockInsuficienteException("Stock insuficiente para el producto: " + producto.getDescripcion());
                }

                int nuevoStock = producto.getStock() - item.getCantidad();
                log.info("Descontando stock para '{}'. Nuevo stock: {}", producto.getDescripcion(), nuevoStock);
                producto.setStock(nuevoStock);
                productoRepository.save(producto);

                Detalle_factura detalle = new Detalle_factura();
                detalle.setProducto(producto);
                detalle.setCantidad(item.getCantidad());
                detalle.setPrecio_unitario(producto.getPrecio());
                detalle.setFactura(facturaGuardada);
                detalles.add(detalle);
            }
            
            detalleFacturaRepository.saveAll(detalles);
            log.info("Guardados {} detalles de la factura.", detalles.size());

            log.info("Vaciando carrito de la base de datos para el usuario ID: {}", usuario.getId());
            carritoService.vaciarCarrito(usuario.getId());
            log.info("Carrito vaciado.");

            log.info("Limpiando datos de la sesión...");
            session.removeAttribute("carrito_mp");
            session.removeAttribute("usuario_mp");
            log.info("Sesión limpiada.");

            model.addAttribute("factura", facturaGuardada);
            model.addAttribute("titulo", "¡Compra Exitosa!");

            log.info("--- PROCESO DE PAGO COMPLETADO EXITOSAMENTE. Mostrando página de éxito. ---");
            return "success";

        } catch (StockInsuficienteException e) {
            log.error("Error de negocio: Stock insuficiente. La transacción hará rollback.", e);
            flash.addFlashAttribute("error", "No se pudo completar la compra: " + e.getMessage());
            throw e; // Relanzar para asegurar rollback
        } catch (Exception e) {
            log.error("Error crítico al procesar la venta en /success. La transacción hará rollback.", e);
            flash.addFlashAttribute("error", "Error crítico al procesar la venta. Contacte a soporte.");
            throw e; // Relanzar para asegurar rollback
        }
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
}