package com.analistas.luzclaritaweb.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.analistas.luzclaritaweb.dto.CarritoDTO;
import com.analistas.luzclaritaweb.model.domain.Factura;
import com.analistas.luzclaritaweb.model.domain.Producto;
import com.analistas.luzclaritaweb.model.domain.Receta;
import com.analistas.luzclaritaweb.model.domain.Usuario;
import com.analistas.luzclaritaweb.web.config.security.CustomUserDetails;
import com.analistas.luzclaritaweb.model.service.interfaces.ICarritoService;
import com.analistas.luzclaritaweb.model.service.interfaces.IProcesamientoPagoService;
import com.analistas.luzclaritaweb.model.service.interfaces.IProductoService;
import com.analistas.luzclaritaweb.web.excepciones.MontoDelPagoNoCoincideException;
import com.analistas.luzclaritaweb.web.excepciones.OrdenYaProcesadaException;
import com.analistas.luzclaritaweb.web.excepciones.PagoNoAprobadoException;
import com.analistas.luzclaritaweb.web.excepciones.StockInsuficienteException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.mercadopago.resources.Payment;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.Preference;
import com.mercadopago.resources.datastructures.preference.BackUrls;
import com.mercadopago.resources.datastructures.preference.Item;
import com.mercadopago.resources.datastructures.preference.Payer;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class MercadoPagoController {

    @Autowired
    private IProductoService productoService;

    @Autowired
    private IProcesamientoPagoService procesamientoPagoService;

    @Autowired
    private ICarritoService carritoService;

    @Autowired
    private com.analistas.luzclaritaweb.model.service.interfaces.IRecetasService recetaService;

    // @Autowired
    // private ICajaRepository cajaRepository;

    @PostMapping("/createAndRedirect")
    @SuppressWarnings("CallToPrintStackTrace")
    public String createAndRedirect(@RequestParam("cartData") String cartDataJson,
            Model model,
            Authentication authentication) throws MPException {

        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            model.addAttribute("error", "Error de autenticación. Por favor, inicie sesión nuevamente.");
            return "redirect:/inicioSesion/login";
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Usuario usuario = userDetails.getUsuario();

        if (usuario == null) {
            model.addAttribute("error", "Error de autenticación. No se pudieron cargar los datos de usuario.");
            return "redirect:/inicioSesion/login";
        }

        try {
            // System.out.println("=== DEBUG AUTHENTICATION ===");
            // System.out.println("Authentication: " + authentication);
            // System.out.println("Principal: " + authentication.getPrincipal());
            // System.out.println("Principal class: " +
            // authentication.getPrincipal().getClass());
            // System.out.println("Authorities: " + authentication.getAuthorities());

            // // Obtener el usuario de manera más robusta
            // Usuario usuario = null;
            // Object principal = authentication.getPrincipal();

            // if (principal instanceof UserDetails) {
            // UserDetails userDetails = (UserDetails) principal;
            // System.out.println("Username from UserDetails: " +
            // userDetails.getUsername());

            // // Tu CustomUserDetails SÍ tiene el método getUsuario()
            // if (principal instanceof
            // com.analistas.luzclaritaweb.web.config.security.CustomUserDetails) {
            // com.analistas.luzclaritaweb.web.config.security.CustomUserDetails
            // customUserDetails =
            // (com.analistas.luzclaritaweb.web.config.security.CustomUserDetails)
            // principal;
            // usuario = customUserDetails.getUsuario();
            // System.out.println("Usuario from CustomUserDetails: " + usuario);
            // }

            // } else if (principal instanceof Usuario) {
            // usuario = (Usuario) principal;
            // System.out.println("Usuario direct cast: " + usuario);
            // }

            // if (usuario == null) {
            // System.err.println("ERROR: No se pudo obtener el usuario autenticado");
            // model.addAttribute("error", "Error de autenticación. Por favor, inicie sesión
            // nuevamente.");
            // return "redirect:/inicioSesion/login";
            // }

            // System.out.println("Usuario ID: " + usuario.getId());
            // System.out.println("Usuario Email: " + usuario.getEmail());
            // System.out.println("=== END DEBUG ===");

            // Deserializar el JSON del carrito
            ObjectMapper objectMapper = new ObjectMapper();
            List<CarritoDTO> carritoItems = objectMapper.readValue(cartDataJson,
                    new TypeReference<List<CarritoDTO>>() {
                    });

            System.out.println("Carrito items count: " + carritoItems.size());

            // Verificar que el carrito no esté vacío
            if (carritoItems.isEmpty()) {
                model.addAttribute("error", "El carrito está vacío");
                return "redirect:/home?error=carrito_vacio";
            }

            // Verificar stock antes de proceder
            for (CarritoDTO item : carritoItems) {
                if (item.getProductoId() != null) {
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
                // Nota: La verificación de stock para recetas no está implementada, ya que las
                // recetas no tienen stock.
            }

            // Crear la preferencia de MercadoPago
            Preference preference = new Preference();
            preference.setBackUrls(new BackUrls()
                    // .setFailure("http://localhost:8081/failure")
                    // .setPending("http://localhost:8081/pending")
                    // .setSuccess("http://localhost:8081/success"));
                    .setFailure("https://fe8484098da6.ngrok-free.app/failure")
                    .setPending("https://fe8484098da6.ngrok-free.app/pending")
                    .setSuccess("https://fe8484098da6.ngrok-free.app/success"));

            // Añadir el pagador (Payer) a la preferencia
            Payer payer = new Payer();
            payer.setEmail(usuario.getEmail());
            preference.setPayer(payer);

            // Procesar los items del carrito para la preferencia de pago
            for (CarritoDTO item : carritoItems) {
                Item mpItem = new Item();
                if (item.getProductoId() != null) {
                    // Es un producto
                    Producto producto = productoService.buscarPorId(item.getProductoId());
                    if (producto != null) {
                        if (producto.getPrecio() == null || producto.getPrecio().floatValue() <= 0) {
                            System.err.println("ERROR DE DIAGNÓSTICO: El producto '" + producto.getDescripcion()
                                    + "' (ID: " + producto.getId() + ") tiene un precio inválido de "
                                    + producto.getPrecio());
                            model.addAttribute("error", "El producto en el carrito '" + producto.getDescripcion()
                                    + "' tiene un precio inválido y no se puede procesar.");
                            return "redirect:/home";
                        }
                        mpItem.setTitle(producto.getDescripcion())
                                .setQuantity(item.getCantidad())
                                .setUnitPrice(producto.getPrecio().floatValue());
                        preference.appendItem(mpItem);
                    }
                } else if (item.getRecetaId() != null) {
                    // Es una receta
                    Receta receta = recetaService.buscarPorId(item.getRecetaId());
                    if (receta != null) {
                        mpItem.setTitle(receta.getNombre_receta())
                                .setQuantity(item.getCantidad())
                                .setUnitPrice(receta.getPrecio().floatValue());
                        preference.appendItem(mpItem);
                    }
                }
            }

            System.out.println("--- DIAGNOSTICO: PREFERENCIA A ENVIAR ---");
            System.out.println(new Gson().toJson(preference));
            System.out.println("------------------------------------------");

            try {
                var result = preference.save();

                System.out.println("--- DIAGNOSTICO: RESPUESTA COMPLETA DE MP ---");
                System.out.println(new Gson().toJson(result));
                System.out.println("---------------------------------------------");

                if (result.getInitPoint() == null) {
                    System.err.println(
                            "ERROR CRÍTICO: El init_point de MercadoPago es nulo incluso después de una respuesta aparentemente exitosa.");
                    System.err.println(
                            "Esto casi siempre indica un problema con el Access Token (inválido, o de un entorno incorrecto - ej. PROD en vez de SANDBOX).");
                    if (result.getLastApiResponse() != null) {
                        System.err.println(
                                "Respuesta cruda de la API: " + result.getLastApiResponse().getStringResponse());
                    }
                    model.addAttribute("error",
                            "Error de configuración con la pasarela de pago. Por favor, contacte al soporte.");
                    return "redirect:/home";
                }

                // System.out.println("MercadoPago preference created. Redirect URL: " + result.getInitPoint());
                // return "redirect:" + result.getInitPoint();
                System.out.println("MercadoPago preference created. Redirect URL: " + result.getSandboxInitPoint());
                return "redirect:" + result.getSandboxInitPoint();

            } catch (MPException e) {
                System.err.println("--- EXCEPCIÓN DE MERCADOPAGO CAPTURADA ---");
                System.err.println("Status Code: " + e.getStatusCode());

                // Corrección: Usar e.toString() para obtener detalles del error
                String errorDetails = e.toString();
                System.err.println("Detalles del error: " + errorDetails);

                // Corrección: Obtener causa del error
                Throwable cause = e.getCause();
                System.err.println("Causa: " + (cause != null ? cause.getMessage() : "N/A"));

                System.err.println("Mensaje: " + e.getMessage());
                e.printStackTrace();
                model.addAttribute("error", "Error al procesar el pago con MercadoPago: " + e.getMessage());
                return "redirect:/home?error=error_mercadopago";
            }

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
            @AuthenticationPrincipal Usuario usuario,
            Model model) {

        try {
            // 1. Verificar el pago con MercadoPago
            Payment payment = Payment.findById(collectionId);
            if (payment == null) {
                model.addAttribute("error", "No se encontró la información del pago en MercadoPago.");
                return "ventas/failure";
            }

            // 2. Obtener los items del carrito desde la base de datos
            List<CarritoDTO> itemsCarrito = carritoService.obtenerCarritoPorUsuario(usuario.getId());
            if (itemsCarrito.isEmpty()) {
                model.addAttribute("error", "El carrito está vacío o ya fue procesado.");
                return "ventas/failure";
            }

            // 3. Delegar toda la lógica de negocio al servicio de procesamiento
            Factura factura = procesamientoPagoService.procesarPagoExitosoMP(
                    itemsCarrito,
                    usuario,
                    payment.getPaymentMethodId(),
                    collectionId,
                    externalReference,
                    java.math.BigDecimal.valueOf(payment.getTransactionAmount()),
                    payment.getStatus().toString());

            // 4. Preparar datos para la vista de éxito
            model.addAttribute("payment", payment);
            model.addAttribute("factura", factura);
            model.addAttribute("titulo", "¡Compra Exitosa!");
            return "ventas/success";

        } catch (MPException e) {
            model.addAttribute("error", "Error al comunicarse con MercadoPago: " + e.getMessage());
            return "ventas/failure";
        } catch (MontoDelPagoNoCoincideException | StockInsuficienteException | OrdenYaProcesadaException
                | PagoNoAprobadoException e) {
            // Capturamos nuestras excepciones de negocio y mostramos un error claro
            model.addAttribute("error", e.getMessage());
            return "ventas/failure";
        } catch (Exception e) {
            // Captura de cualquier otro error inesperado
            model.addAttribute("error", "Ocurrió un error inesperado al procesar su compra.");
            e.printStackTrace(); // Loguear el error para depuración
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
            Model model) throws MPException {

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
            Model model) throws MPException {

        model.addAttribute("titulo", "Pago Pendiente");
        model.addAttribute("mensaje", "Su pago está siendo procesado. Le notificaremos cuando sea aprobado.");

        return "ventas/pending";
    }

}

// // @GetMapping("/success")
// // public String success(@RequestParam(name = "payment_id", defaultValue =
// "0")
// // Long paymentId,
// // @RequestParam(name = "status", required = false) String status,
// // @RequestParam(name = "external_reference", required = false) String
// // externalReference,
// // @RequestParam(name = "merchant_order_id", required = false) String
// // merchantOrderId,
// // @RequestParam(name = "payment_type", required = false) String paymentType,
// // @RequestParam(name = "preference_id", required = false) String
// preferenceId,
// // @ModelAttribute("venta") Venta venta, @ModelAttribute("pedido") Pedido
// // pedido,
// // String merchantAccountId, Model model, SessionStatus sessionStatus,
// // HttpSession session)
// // throws MPException {

// // model.addAttribute("titulo", "¡Muchas Gracias!");
// // model.addAttribute("paymentId", paymentId);
// // model.addAttribute("status", status);
// // model.addAttribute("externalReference", externalReference);
// // model.addAttribute("merchantOrderId", merchantOrderId);

// // if (status.equals("approved")) {
// // System.out.println("Guardando en la base de datos... ");

// // // Descontar stock de los productos del pedido
// // for (Map.Entry<Producto, Integer> entry :
// pedido.getProductos().entrySet()) {
// // Producto producto = entry.getKey();
// // int cantidadVendida = entry.getValue();

// // if (producto.getStock() >= cantidadVendida) {
// // producto.setStock(producto.getStock() - cantidadVendida);
// // productoService.guardar(producto); // ✅ Guardar cambios en la base de
// datos
// // } else {
// // model.addAttribute("error", "Error: No hay suficiente stock para " +
// // producto.getDescripcion());
// // return "home/home";
// // }
// // }

// // ventaService.guardar(venta);
// // // Hasta acá se guardó las lineas de ventas en la venta, y tambien la
// linea
// // de
// // // venta
// // // Ahora guardamos el movimiento tambien:

// // Movimiento movimiento = new Movimiento();
// // movimiento.setVenta(venta);
// // movimientoService.guardar(movimiento);

// // // Guardar el pedido en la base de datos
// // pedidoService.guardar(pedido);
// // }

// // session.removeAttribute("pedido");
// // sessionStatus.setComplete();

// // return "success";
// // }

// // @GetMapping("/createAndRedirect")
// // public String createAndRecirect(@RequestParam("item_ids[]") List<String>
// // itemIds,
// // @RequestParam("cantidad_[]") List<String> cantidades, @Valid Venta venta,
// // Model model, HttpSession session)
// // throws MPException {

// // Preference preference = new Preference();

// // preference.setBackUrls(new
// // BackUrls().setFailure("http://localhost:8081/error")
// //
// .setPending("http://localhost:8081/pending").setSuccess("http://localhost:8081/success"));

// // preference.setAutoReturn(Preference.AutoReturn.approved);

// // // Aquíe se crea un item del pago, o una colección de
// // // items, que se pueden enviar como parámetro del método
// // createAndRedirect()...

// // double total = 0;

// // for (int i = 0; i < itemIds.size(); i++) {
// // Long id = Long.parseLong(itemIds.get(i));
// // int cant = Integer.parseInt(cantidades.get(i));

// // Producto producto = productoService.buscarPorId(id);

// // if (producto != null) {
// // Item item = new Item();
// // item.setTitle(producto.getDescripcion())
// // .setQuantity(cant)
// // .setUnitPrice((float) producto.getPrecioMin());

// // // System.out.println(producto.getDescripcion());
// // total += cant * producto.getPrecioMin();
// // preference.appendItem(item);

// // // Verificar de nuevo si hay stock
// // if (cant > producto.getStock()) {
// // model.addAttribute("titulo", "Nueva Venta");
// // model.addAttribute("warning", "No hay stock suficiente...");
// // return "ventas/form";
// // }

// // LineaVenta lineaVenta;
// // lineaVenta = new LineaVenta();

// // lineaVenta.setProducto(producto);
// // lineaVenta.setPrecioActual(producto.getPrecioMin());
// // lineaVenta.setCantidad(cant);

// // // Importante: actualizar stock del producto...
// // // producto.setStock(producto.getStock() - cant);

// // venta.addLinea(lineaVenta);
// // }

// // }

// // venta.setDescripcionGral("Compra Online");
// // MetodoPago metodoPago = metodoPagoService.buscarPorId(2L);
// // venta.setMetodoPago(metodoPago);
// // venta.setFechaHora(LocalDateTime.now());
// // venta.setTotal(total);

// // Usuario usuario = usuarioService.buscarPorId(1L);
// // venta.setUsuario(usuario);
// // // Obtener el usuario que realizo la compra online para guardarla en la
// // venta:
// // Authentication authentication =
// // SecurityContextHolder.getContext().getAuthentication();
// // if (authentication != null && authentication.isAuthenticated()) {
// // Object principal = authentication.getPrincipal();

// // if (principal instanceof UserDetails) {
// // String username = ((UserDetails) principal).getUsername();

// // Cliente cliente = clienteService.findByDni(username);
// // if (cliente != null) {
// // venta.setCliente(cliente);
// // System.out.println(cliente);
// // } else {
// // model.addAttribute("error", "No se encontró el cliente");
// // return "home/home";
// // }
// // }
// // }

// // // Una vez generado la venta y el movimiento solo queda generar el pedido
// // online
// // // Crear el pedido
// // Pedido pedido = new Pedido();
// // pedido.setCliente(venta.getCliente());

// // Map<Producto, Integer> productosPedido = new HashMap<>();
// // Producto producto;
// // for (int i = 0; i < itemIds.size(); i++) {
// // Long id = Long.parseLong(itemIds.get(i));
// // int cantidad = Integer.parseInt(cantidades.get(i));

// // producto = productoService.buscarPorId(id);
// // if (producto != null) {
// // productosPedido.put(producto, cantidad);
// // }
// // }

// // // Asignar los productos al pedido
// // pedido.setProductos(productosPedido);
// // pedido.setTotal(total);

// // // Item item = new Item();
// // // item.setTitle("Cerveza Patagonia").setQuantity(1).setUnitPrice((float)
// // 0.50);
// // // preference.appendItem(item);

// // var result = preference.save();

// // model.addAttribute("venta", venta);
// // // model.addAttribute("pedido", pedido);
// // session.setAttribute("pedido", pedido);

// // System.out.println(result.getInitPoint());
// // return "redirect:" + result.getInitPoint();
// // }

// // @GetMapping("/success")
// // public String success(@RequestParam(name = "payment_id", defaultValue =
// "0")
// // Long paymentId,
// // @RequestParam(name = "status", required = false) String status,
// // @RequestParam(name = "external_reference", required = false) String
// // externalReference,
// // @RequestParam(name = "merchant_order_id", required = false) String
// // merchantOrderId,
// // @RequestParam(name = "payment_type", required = false) String paymentType,
// // @RequestParam(name = "preference_id", required = false) String
// preferenceId,
// // @ModelAttribute("venta") Venta venta, @ModelAttribute("pedido") Pedido
// // pedido,
// // String merchantAccountId, Model model, SessionStatus sessionStatus,
// // HttpSession session)
// // throws MPException {

// // model.addAttribute("titulo", "¡Muchas Gracias!");
// // model.addAttribute("paymentId", paymentId);
// // model.addAttribute("status", status);
// // model.addAttribute("externalReference", externalReference);
// // model.addAttribute("merchantOrderId", merchantOrderId);

// // if (status.equals("approved")) {
// // System.out.println("Guardando en la base de datos... ");

// // // Descontar stock de los productos del pedido
// // for (Map.Entry<Producto, Integer> entry :
// pedido.getProductos().entrySet()) {
// // Producto producto = entry.getKey();
// // int cantidadVendida = entry.getValue();

// // if (producto.getStock() >= cantidadVendida) {
// // producto.setStock(producto.getStock() - cantidadVendida);
// // productoService.guardar(producto); // ✅ Guardar cambios en la base de
// datos
// // } else {
// // model.addAttribute("error", "Error: No hay suficiente stock para " +
// // producto.getDescripcion());
// // return "home/home";
// // }
// // }

// // ventaService.guardar(venta);
// // // Hasta acá se guardó las lineas de ventas en la venta, y tambien la
// linea
// // de
// // // venta
// // // Ahora guardamos el movimiento tambien:

// // Movimiento movimiento = new Movimiento();
// // movimiento.setVenta(venta);
// // movimientoService.guardar(movimiento);

// // // Guardar el pedido en la base de datos
// // pedidoService.guardar(pedido);
// // }

// // session.removeAttribute("pedido");
// // sessionStatus.setComplete();

// // return "success";
// // }
