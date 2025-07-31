package com.analistas.luzclaritaweb.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.analistas.luzclaritaweb.model.domain.Carrito;
import com.analistas.luzclaritaweb.model.domain.Factura;
import com.analistas.luzclaritaweb.model.domain.Producto;
import com.analistas.luzclaritaweb.model.domain.Usuario;
import com.analistas.luzclaritaweb.model.service.interfaces.ICarritoService;
import com.analistas.luzclaritaweb.model.service.interfaces.IFacturaService;
import com.analistas.luzclaritaweb.model.service.interfaces.IProductoService;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.Preference;
import com.mercadopago.resources.datastructures.preference.BackUrls;
import com.mercadopago.resources.datastructures.preference.Item;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class MercadoPagoController {

    @Autowired
    private IProductoService productoService;

    // Registros de Movimientos:
    @Autowired
    private IFacturaService facturaService; // Inyección añadida

    //@Autowired
    //private IMovimientoCajaService movimientoCajaService;

    @Autowired
    private ICarritoService carritoService;

    // @Autowired
    // private ICajaRepository cajaRepository;

    @GetMapping("/createAndRedirect")
    public String createAndRedirect(@RequestParam("item_ids[]") List<Long> itemIds,
                                  @RequestParam("cantidad_[]") List<Integer> cantidades,
                                  Model model,
                                  @AuthenticationPrincipal Usuario usuario) throws MPException {

        // Verificar stock antes de proceder
        for (int i = 0; i < itemIds.size(); i++) {
            Producto producto = productoService.buscarPorId(itemIds.get(i));
            if (producto.getStock() < cantidades.get(i)) {
                model.addAttribute("error", "No hay suficiente stock para " + producto.getDescripcion());
                return "redirect:/carrito"; // Redirige de vuelta al carrito con mensaje de error
            }
        }

        Preference preference = new Preference();
        preference.setBackUrls(new BackUrls()
                .setFailure("http://localhost:8081/failure")
                .setPending("http://localhost:8081/pending")
                .setSuccess("http://localhost:8081/success"));

        // Procesar los productos para la preferencia de pago
        for (int i = 0; i < itemIds.size(); i++) {
            Long id = itemIds.get(i);
            int cantidad = cantidades.get(i);

            Producto producto = productoService.buscarPorId(id);
            if (producto != null) {
                Item item = new Item();
                item.setTitle(producto.getDescripcion())
                    .setQuantity(cantidad)
                    .setUnitPrice(producto.getPrecio().floatValue());

                preference.appendItem(item);
            }
        }

        var result = preference.save();
        return "redirect:" + result.getInitPoint();
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
                        Model model) throws MPException {
        
        // Verificar el pago con MercadoPago
        var payment = com.mercadopago.resources.Payment.findById(collectionId);
        
        if (!"approved".equals(collectionStatus)) {
            return "redirect:/failure";
        }

        // Obtener items del carrito
        List<Carrito> itemsCarrito = carritoService.obtenerCarritoPorUsuario(usuario.getId());
        
        // Crear factura
        Factura factura = facturaService.crearFacturaDesdeCarrito(itemsCarrito, usuario, "MercadoPago");
        
        // Registrar movimiento de caja (ingreso)
        //movimientoCajaService.registrarMovimiento(factura, usuario);
        
        // Actualizar inventario (restar stock)
        facturaService.actualizarInventario(itemsCarrito);
        
        // Vaciar carrito
        carritoService.vaciarCarrito(usuario.getId());
        
        // Preparar datos para la vista
        model.addAttribute("payment", payment);
        model.addAttribute("factura", factura);
        model.addAttribute("titulo", "¡Compra Exitosa!");
        
        return "ventas/success";
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
// // public String success(@RequestParam(name = "payment_id", defaultValue = "0")
// // Long paymentId,
// // @RequestParam(name = "status", required = false) String status,
// // @RequestParam(name = "external_reference", required = false) String
// // externalReference,
// // @RequestParam(name = "merchant_order_id", required = false) String
// // merchantOrderId,
// // @RequestParam(name = "payment_type", required = false) String paymentType,
// // @RequestParam(name = "preference_id", required = false) String preferenceId,
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
// // for (Map.Entry<Producto, Integer> entry : pedido.getProductos().entrySet()) {
// // Producto producto = entry.getKey();
// // int cantidadVendida = entry.getValue();

// // if (producto.getStock() >= cantidadVendida) {
// // producto.setStock(producto.getStock() - cantidadVendida);
// // productoService.guardar(producto); // ✅ Guardar cambios en la base de datos
// // } else {
// // model.addAttribute("error", "Error: No hay suficiente stock para " +
// // producto.getDescripcion());
// // return "home/home";
// // }
// // }

// // ventaService.guardar(venta);
// // // Hasta acá se guardó las lineas de ventas en la venta, y tambien la linea
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
// // .setPending("http://localhost:8081/pending").setSuccess("http://localhost:8081/success"));

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
// // public String success(@RequestParam(name = "payment_id", defaultValue = "0")
// // Long paymentId,
// // @RequestParam(name = "status", required = false) String status,
// // @RequestParam(name = "external_reference", required = false) String
// // externalReference,
// // @RequestParam(name = "merchant_order_id", required = false) String
// // merchantOrderId,
// // @RequestParam(name = "payment_type", required = false) String paymentType,
// // @RequestParam(name = "preference_id", required = false) String preferenceId,
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
// // for (Map.Entry<Producto, Integer> entry : pedido.getProductos().entrySet()) {
// // Producto producto = entry.getKey();
// // int cantidadVendida = entry.getValue();

// // if (producto.getStock() >= cantidadVendida) {
// // producto.setStock(producto.getStock() - cantidadVendida);
// // productoService.guardar(producto); // ✅ Guardar cambios en la base de datos
// // } else {
// // model.addAttribute("error", "Error: No hay suficiente stock para " +
// // producto.getDescripcion());
// // return "home/home";
// // }
// // }

// // ventaService.guardar(venta);
// // // Hasta acá se guardó las lineas de ventas en la venta, y tambien la linea
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
