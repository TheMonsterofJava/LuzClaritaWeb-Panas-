package com.analistas.luzclaritaweb.web.controller;

import java.awt.Color;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.analistas.luzclaritaweb.model.domain.Caja;
import com.analistas.luzclaritaweb.model.domain.MovimientoCaja;
import com.analistas.luzclaritaweb.model.domain.Usuario;
import com.analistas.luzclaritaweb.model.service.interfaces.ICajaService;
import com.analistas.luzclaritaweb.model.service.interfaces.IMovimientoCajaService;
import com.analistas.luzclaritaweb.model.service.interfaces.IUsuariosService;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/caja")
public class CajaController {

    private final PasswordEncoder passwordEncoder;

    @Autowired
    private ICajaService cajaService;

    @Autowired
    private IMovimientoCajaService movimientoCajaService;

    @Autowired
    private IUsuariosService usuarioService;

    // Constructor para inyectar PasswordEncoder
    public CajaController(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    // Listar todas las cajas
    @GetMapping("/listado")
    public String listadoCajas(Model model) {
        List<Caja> cajas = cajaService.listarCajas();
        model.addAttribute("titulo", "Administración de Cajas");
        model.addAttribute("cajas", cajas);
        return "movimientos/caja";
    }

    // Ver detalle de una caja
    @GetMapping("/detalle/{id}")
    public String detalleCaja(@PathVariable("id") Long id, Model model) {
        Optional<Caja> cajaOpt = cajaService.obtenerCajaPorId(id);
        if (cajaOpt.isEmpty()) {
            model.addAttribute("error", "Caja no encontrada.");
            return "redirect:/caja/listado";
        }
        Caja caja = cajaOpt.get();
        model.addAttribute("caja", caja);
        model.addAttribute("movimientos", movimientoCajaService.buscarPorCaja(id));
        return "movimientos/detalle_caja"; // crea esta vista si quieres detalle extendido
    }

    // Abrir una caja
    @GetMapping("/nueva")
    public String nuevaCajaForm(Model model) {
        model.addAttribute("caja", new Caja());
        return "movimientos/caja_form";
    }

    // Guardar una nueva caja
    // Aquí se asocia el usuario autenticado a la caja
    // Si el usuario no está autenticado, se maneja el error y se redirige
    // adecuadamente.
    @PostMapping("/guardar")
    public String guardarCaja(@ModelAttribute Caja caja, RedirectAttributes flash, Authentication authentication) {

        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            // Asumiendo que buscarPorNombreUsuario devuelve Optional<Usuario>
            Usuario usuarioLogueado = usuarioService.findByEmail(userDetails.getUsername()).orElse(null);

            if (usuarioLogueado != null) {
                caja.setUsuario(usuarioLogueado);
            } else {
                // Usuario no encontrado en la BD, aunque esté autenticado.
                // Esto sería un estado inconsistente del sistema.
                flash.addFlashAttribute("error",
                        "El usuario autenticado no pudo ser encontrado en el sistema para asociarlo a la caja.");
                // Dependiendo de las reglas de negocio, podrías querer impedir que la caja se
                // guarde:
                // return "redirect:/caja/nueva"; // O a donde sea apropiado
            }
        } else {
            // No hay información de autenticación o no es del tipo esperado.
            // Esto podría pasar si el endpoint no está debidamente protegido o hay un
            // problema con la configuración de seguridad.
            flash.addFlashAttribute("error",
                    "No se pudo obtener la información de autenticación para asociar un usuario a la caja.");
            // De nuevo, considera si esto debe impedir guardar la caja.
            // return "redirect:/caja/nueva";
        }

        caja.setEstado(Caja.EstadoCaja.ABIERTA);
        caja.setSaldoFinal(caja.getSaldoInicial());
        cajaService.guardarCaja(caja);

        // Solo mostrar el success si no hubo errores previos al intentar asignar
        // usuario.
        if (!flash.getFlashAttributes().containsKey("error")) {
            flash.addFlashAttribute("success", "Caja abierta correctamente.");
        }
        return "redirect:/caja/listado";
    }

    // Cerrar una caja
    @GetMapping("/cerrar/{id}")
    public String cerrarCaja(@PathVariable("id") Long id, RedirectAttributes flash) {
        Optional<Caja> cajaOpt = cajaService.obtenerCajaPorId(id);
        if (cajaOpt.isPresent()) {
            Caja caja = cajaOpt.get();
            caja.setEstado(Caja.EstadoCaja.CERRADA);
            cajaService.guardarCaja(caja);
            flash.addFlashAttribute("success", "Caja cerrada correctamente.");
        } else {
            flash.addFlashAttribute("error", "No se encontró la caja.");
        }
        return "redirect:/caja/listado";
    }

    // Abrir una caja cerrada (re-abrir)
    @GetMapping("/abrir/{id}")
    public String abrirCaja(@PathVariable("id") Long id, RedirectAttributes flash) {
        Optional<Caja> cajaOpt = cajaService.obtenerCajaPorId(id);
        if (cajaOpt.isPresent()) {
            Caja caja = cajaOpt.get();
            caja.setEstado(Caja.EstadoCaja.ABIERTA);
            cajaService.guardarCaja(caja);
            flash.addFlashAttribute("success", "Caja abierta nuevamente.");
        } else {
            flash.addFlashAttribute("error", "No se encontró la caja.");
        }
        return "redirect:/caja/listado";
    }

    // Historial de movimientos de una caja específica
    @GetMapping("/movimientos")
    public String movimientosPorCaja(
            @RequestParam(value = "cajaId", required = false) Long cajaId,
            @RequestParam(name = "fechaInicio", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaInicio,
            @RequestParam(name = "fechaFin", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaFin,
            Model model) {

        if (cajaId == null) {
            model.addAttribute("error", "Debe seleccionar una caja.");
            return "redirect:/caja/listado";
        }

        LocalDateTime inicio = (fechaInicio != null) ? fechaInicio.atStartOfDay()
                : LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime fin = (fechaFin != null) ? fechaFin.atTime(23, 59, 59) : LocalDateTime.now();

        // 1. Obtén la caja
        Optional<Caja> cajaOpt = cajaService.obtenerCajaPorId(cajaId);
        if (cajaOpt.isEmpty()) {
            model.addAttribute("error", "Caja no encontrada con ID: " + cajaId);
            // Considera redirigir a una página de error o listado
            return "redirect:/caja/listado";
        }
        Caja caja = cajaOpt.get();

        // 2. Obtén el saldo inicial (ahora es BigDecimal directamente)
        BigDecimal saldoInicial = caja.getSaldoInicial() != null ? caja.getSaldoInicial() : BigDecimal.ZERO;

        // Obtener todos los movimientos de la caja
        List<MovimientoCaja> todosLosMovimientos = movimientoCajaService.buscarPorCaja(cajaId);

        // 3. Calcula ingresos y egresos (filtrando por fecha)
        List<MovimientoCaja> movimientosFiltrados = todosLosMovimientos.stream()
                .filter(mov -> !mov.getFecha().isBefore(inicio) && !mov.getFecha().isAfter(fin))
                .collect(Collectors.toList());

        BigDecimal totalIngresos = BigDecimal.ZERO;
        BigDecimal totalEgresos = BigDecimal.ZERO;

        for (MovimientoCaja mov : movimientosFiltrados) {
            if (mov.getMonto() != null) { // Asegurarse que el monto no sea nulo
                if (MovimientoCaja.TipoOperacion.INGRESO.equals(mov.getTipoOperacion())) {
                    totalIngresos = totalIngresos.add(mov.getMonto());
                } else if (MovimientoCaja.TipoOperacion.EGRESO.equals(mov.getTipoOperacion())) {
                    totalEgresos = totalEgresos.add(mov.getMonto());
                }
            }
        }

        // 4. Calcula el saldo actual
        BigDecimal saldoActual = saldoInicial.add(totalIngresos).subtract(totalEgresos);

        model.addAttribute("movimientos", movimientosFiltrados); // Usar movimientos filtrados para la tabla
        model.addAttribute("cajaId", cajaId);
        model.addAttribute("fechaInicio", inicio.toLocalDate());
        model.addAttribute("fechaFin", fin.toLocalDate());
        model.addAttribute("titulo", "Movimientos de Caja - " + caja.getId()); // Añadir ID de caja al título

        // 5. Pasa los datos a la vista
        Map<String, Object> resumen = new HashMap<>();
        resumen.put("ingresos", totalIngresos); // Mantener como BigDecimal
        resumen.put("egresos", totalEgresos); // Mantener como BigDecimal
        resumen.put("saldo", saldoActual); // Mantener como BigDecimal
        model.addAttribute("resumen", resumen);

        return "movimientos/movimientos_caja";
    }

    // Exportar movimientos de caja a CSV (endpoint ejemplo)
    @GetMapping("/exportar")
    public void exportarCsv(@RequestParam("cajaId") Long cajaId, javax.servlet.http.HttpServletResponse response)
            throws IOException {
        List<MovimientoCaja> movimientos = movimientoCajaService.buscarPorCaja(cajaId);
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=movimientos_caja_" + cajaId + ".csv");

        try (java.io.PrintWriter writer = response.getWriter()) {
            writer.println("Fecha,Tipo,Monto,Descripción,Usuario");

            for (MovimientoCaja mov : movimientos) {
                writer.printf("%s,%s,%s,%s,%s\n",
                        mov.getFecha(),
                        mov.getTipo(),
                        mov.getMonto(),
                        mov.getDescripcion(),
                        mov.getOperador() != null ? mov.getOperador().getNomb_usu() : "-");
            }
            writer.flush();
        }
    }

    @GetMapping("/exportar/pdf")
    public void exportarPdf(
            HttpServletResponse response,
            @RequestParam("cajaId") Long cajaId,
            @RequestParam(name = "fechaInicio", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaInicioParam,
            @RequestParam(name = "fechaFin", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaFinParam) {

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=movimientos_caja_" + cajaId + ".pdf");

        LocalDateTime fechaInicio = (fechaInicioParam != null) ? fechaInicioParam.atStartOfDay()
                : LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime fechaFin = (fechaFinParam != null) ? fechaFinParam.atTime(23, 59, 59) : LocalDateTime.now();

        Optional<Caja> cajaOpt = cajaService.obtenerCajaPorId(cajaId);
        if (cajaOpt.isEmpty()) {
            // Manejar caja no encontrada, quizás loguear o enviar un error HTTP si es
            // posible
            // Como es un void method y el response header ya está seteado, es complejo
            // enviar un error HTML aquí.
            // Podríamos lanzar una excepción que sea manejada globalmente.
            // Por ahora, si la caja no existe, el PDF estará vacío o mostrará un mensaje.
            try (Document document = new Document(PageSize.A4)) {
                PdfWriter.getInstance(document, response.getOutputStream());
                document.open();
                document.add(new Paragraph("Error: Caja no encontrada con ID: " + cajaId));
            } catch (Exception e) {
                // Log error
            }
            return;
        }
        Caja caja = cajaOpt.get();
        // Saldo inicial es BigDecimal directamente
        BigDecimal saldoInicial = caja.getSaldoInicial() != null ? caja.getSaldoInicial() : BigDecimal.ZERO;

        List<MovimientoCaja> todosLosMovimientos = movimientoCajaService.buscarPorCaja(cajaId);
        List<MovimientoCaja> movimientosFiltrados = todosLosMovimientos.stream()
                .filter(mov -> !mov.getFecha().isBefore(fechaInicio) && !mov.getFecha().isAfter(fechaFin))
                .collect(Collectors.toList());

        BigDecimal totalIngresos = BigDecimal.ZERO;
        BigDecimal totalEgresos = BigDecimal.ZERO;
        for (MovimientoCaja mov : movimientosFiltrados) {
            if (mov.getMonto() != null) {
                if (MovimientoCaja.TipoOperacion.INGRESO.equals(mov.getTipoOperacion())) {
                    totalIngresos = totalIngresos.add(mov.getMonto());
                } else if (MovimientoCaja.TipoOperacion.EGRESO.equals(mov.getTipoOperacion())) {
                    totalEgresos = totalEgresos.add(mov.getMonto());
                }
            }
        }
        BigDecimal saldoActual = saldoInicial.add(totalIngresos).subtract(totalEgresos);

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        try (Document document = new Document(PageSize.A4)) {
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD);
            Font bodyFont = new Font(Font.HELVETICA, 10, Font.NORMAL);
            Font totalsFont = new Font(Font.HELVETICA, 12, Font.BOLDITALIC);

            document.add(new Paragraph("Reporte de Movimientos - Caja #" + cajaId, titleFont));
            document.add(new Paragraph(" ")); // Espacio
            document.add(new Paragraph(
                    "Periodo: " + fechaInicio.format(dateFormatter) + " - " + fechaFin.format(dateFormatter),
                    bodyFont));
            document.add(new Paragraph(" ")); // Espacio

            document.add(new Paragraph("Saldo Inicial: $" + String.format("%,.2f", saldoInicial), totalsFont));
            document.add(new Paragraph("Total Ingresos: $" + String.format("%,.2f", totalIngresos), totalsFont));
            document.add(new Paragraph("Total Egresos: $" + String.format("%,.2f", totalEgresos), totalsFont));
            document.add(new Paragraph("Saldo Actual: $" + String.format("%,.2f", saldoActual), totalsFont));
            document.add(new Paragraph(" ")); // Espacio

            PdfPTable table = new PdfPTable(6); // 6 Columnas
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            float[] columnWidths = { 2.5f, 1.5f, 1.5f, 3f, 2f, 2.5f };
            table.setWidths(columnWidths);

            String[] headers = { "Fecha/Hora", "Tipo", "Monto", "Descripción", "Operador", "Relación" };
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                cell.setBackgroundColor(Color.LIGHT_GRAY);
                table.addCell(cell);
            }

            for (MovimientoCaja mov : movimientosFiltrados) {
                table.addCell(new Phrase(mov.getFecha().format(dateTimeFormatter), bodyFont));
                table.addCell(new Phrase(mov.getTipoOperacion().name(), bodyFont));
                PdfPCell montoCell = new PdfPCell(new Phrase("$" + String.format("%,.2f", mov.getMonto()), bodyFont));
                montoCell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                table.addCell(montoCell);
                table.addCell(new Phrase(mov.getDescripcion() != null ? mov.getDescripcion() : "", bodyFont));
                table.addCell(new Phrase(mov.getOperador() != null && mov.getOperador().getNomb_usu() != null
                        ? mov.getOperador().getNomb_usu()
                        : "N/A", bodyFont));

                String relacion = "";
                if (mov.getFactura() != null) {
                    relacion = "Factura #" + mov.getFactura().getNumero_factura();
                } else if (mov.getCompra() != null) {
                    relacion = "Compra"; // Descripción más detallada podría ser muy larga para PDF
                }
                table.addCell(new Phrase(relacion, bodyFont));
            }

            document.add(table);
            document.close();

        } catch (DocumentException | IOException e) {
            // Loggear la excepción
            // Considerar cómo notificar al usuario si es posible.
            // e.printStackTrace(); // Para debug, no usar en producción directamente
        }
    }

    // Verificar si una caja tiene movimientos
    @GetMapping("/tiene-movimientos/{id}")
    @ResponseBody
    public Map<String, Boolean> tieneMovimientos(@PathVariable("id") Long id) {
        Map<String, Boolean> response = new HashMap<>();
        Optional<Caja> cajaOpt = cajaService.obtenerCajaPorId(id); // Obtener la caja por ID
        if (cajaOpt.isPresent()) {
            List<MovimientoCaja> movimientos = movimientoCajaService.buscarPorCaja(id);
            response.put("tieneMovimientos", !movimientos.isEmpty());
        } else {
            response.put("tieneMovimientos", false); // Caja no encontrada o inactiva
        }
        return response;
    }

    // Desactivar una caja (borrado lógico)
    @GetMapping("/eliminar/{id}")
    public String desactivarCaja(@PathVariable("id") Long id, RedirectAttributes flash, Authentication authentication) {

        Optional<Caja> cajaOpt = cajaService.obtenerCajaPorId(id);
        if (cajaOpt.isEmpty()) {
            flash.addFlashAttribute("error", "Caja no encontrada o ya está inactiva.");
            return "redirect:/caja/listado";
        }

        List<MovimientoCaja> movimientos = movimientoCajaService.buscarPorCaja(id);
        if (!movimientos.isEmpty()) {
            flash.addFlashAttribute("error",
                    "Esta caja tiene movimientos. Use la opción de desactivación administrativa.");
            return "redirect:/caja/listado";
        }

        try {
            cajaService.desactivarCaja(id);
            flash.addFlashAttribute("success", "Caja desactivada correctamente.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al desactivar la caja.");

        }
        return "redirect:/caja/listado";
    }

    @PostMapping("/desactivar-admin/{id}")
    @ResponseBody
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> desactivarCajaAdmin(@PathVariable("id") Long id,
            @RequestParam("password") String password, Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Usuario adminUsuario = usuarioService.buscarPorNombreUsuario(userDetails.getUsername())
                .orElse(null);

        if (adminUsuario == null) {
            response.put("success", false);
            response.put("message", "Error: Administrador no encontrado.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        if (!passwordEncoder.matches(password, adminUsuario.getClave())) {
            response.put("success", false);
            response.put("message", "Contraseña de administrador incorrecta.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        Optional<Caja> cajaOpt = cajaService.obtenerCajaPorId(id);
        if (cajaOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Caja no encontrada o ya está inactiva.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        try {
            cajaService.desactivarCaja(id);
            response.put("success", true);
            response.put("message", "Caja desactivada correctamente por administrador.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al desactivar la caja: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Cambios:
    // 1. **Entidad Caja**:
    // - Añadí el campo `boolean activa` (default `true`) a `Caja.java`
    // para marcar el estado activo/inactivo de una caja.

    // 2. **Repositorio y Servicio (ICajaRepository, ICajaService,
    // ICajaServiceImpl)**:
    // - Añadí nuevos métodos al repositorio para buscar cajas
    // filtrando por el estado `activa` (ej. `findByActivaTrue`,
    // `findByEstadoAndActivaTrue`, `findByIdAndActivaTrue`,
    // `findTopByEstadoAndActivaTrueOrderByFechaDesc`).
    // - El método de servicio `eliminarCaja` fue renombrado a `desactivarCaja`
    // y modifiqué su lógica para establecer `activa = false` en la
    // caja especificada.
    // - Actualicé los servicios existentes para listar y obtener cajas
    // (`listarCajas`,
    // `listarCajasAbiertas`, `obtenerCajaPorId`,
    // `buscarUltimaCajaAbiertaYActiva`) para
    // respetar el flag `activa`, mostrando/operando solo con cajas activas.

    // 3. **Controlador (CajaController)**:
    // - Añadí un endpoint GET `/caja/tiene-movimientos/{id}` que
    // devuelve JSON indicando si una caja tiene movimientos.
    // - Modifiqué el endpoint GET `/caja/eliminar/{id}` para llamar al
    // nuevo servicio `desactivarCaja`. Se usa para desactivar cajas
    // que no tienen movimientos, previa confirmación tuya desde el frontend.
    // - Añadí un nuevo endpoint POST `/caja/desactivar-admin/{id}`,
    // protegido con `@PreAuthorize("hasRole('ROLE_ADMIN')")`. Este
    // endpoint permite desactivar una caja (típicamente con movimientos)
    // tras verificar tu contraseña de administrador autenticado. Devuelve
    // JSON con el resultado de la operación.

    // 4. **Frontend (movimientos/caja.html)**:
    // - El botón "Eliminar" ahora invoca una función JavaScript.
    // - La función JavaScript `intentarDesactivarCaja(cajaId)` utiliza
    // SweetAlert2 para orquestar el flujo:
    // - Primero, consulta al backend si la caja tiene movimientos.
    // - Si no hay movimientos, te pide una confirmación simple para desactivar.
    // - Si hay movimientos, te pide confirmación y la contraseña del
    // administrador para proceder con la desactivación.
    // - Maneja las respuestas AJAX para mostrarte mensajes de éxito/error.

    // 5. **CSRF Global**:
    // - Me aseguré de que la configuración `$.ajaxSetup` para incluir tokens
    // CSRF en las solicitudes AJAX esté en el layout principal
    // (`layout/layout.html`), haciéndola global para todas las páginas.

    // @GetMapping("/eliminar/{id}")
    // public String eliminarCaja(@PathVariable("id") Long id, RedirectAttributes
    // flash) {
    // try {
    // // Primero verificar si la caja tiene movimientos asociados
    // // Esta es una simplificación. Una lógica más robusta podría ser necesaria
    // // dependiendo de las reglas de negocio (ej. no permitir eliminar si hay
    // // movimientos).
    // // Por ahora, simplemente intentamos eliminar.
    // cajaService.eliminarCaja(id);
    // flash.addFlashAttribute("success", "Caja eliminada correctamente.");
    // } catch (DataIntegrityViolationException e) {
    // flash.addFlashAttribute("error",
    // "No se puede eliminar la caja porque tiene movimientos asociados o está
    // referenciada en otras transacciones.");
    // // Log the exception for server-side analysis
    // // import org.slf4j.Logger;
    // // import org.slf4j.LoggerFactory;
    // // private static final Logger logger =
    // // LoggerFactory.getLogger(CajaController.class);
    // // logger.error("Error al intentar eliminar caja ID {}: {}", id,
    // // e.getMessage());
    // } catch (Exception e) {
    // flash.addFlashAttribute("error", "Error al eliminar la caja.");
    // // logger.error("Error general al intentar eliminar caja ID {}: {}", id,
    // // e.getMessage());
    // }
    // return "redirect:/caja/listado";
    // }
}