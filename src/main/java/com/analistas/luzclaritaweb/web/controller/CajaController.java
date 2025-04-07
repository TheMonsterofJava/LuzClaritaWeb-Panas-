// package com.analistas.luzclaritaweb.web.controller;

// import java.time.LocalDateTime;
// import java.util.List;
// import java.util.Map;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Controller;
// import org.springframework.ui.Model;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.ResponseBody;

// import com.analistas.luzclaritaweb.model.domain.Movimiento_Caja;
// import com.analistas.luzclaritaweb.model.repository.IMovimientoCajaRepository;
// import com.analistas.luzclaritaweb.model.service.IMovimientoCajaService;



// @Controller
// public class CajaController {


//     @Autowired
//     private IMovimientoCajaService movimientoCajaService;

//     @Autowired
//     private IMovimientoCajaRepository movimientoCajaRepository;

//      @GetMapping("/movimientos")
//     public String listarMovimientos(@RequestParam(required = false) String fechaInicio,
//                                    @RequestParam(required = false) String fechaFin,
//                                    Model model) {
        
//         LocalDateTime inicio = fechaInicio != null ? 
//             LocalDateTime.parse(fechaInicio + "T00:00:00") : 
//             LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        
//         LocalDateTime fin = fechaFin != null ? 
//             LocalDateTime.parse(fechaFin + "T23:59:59") : 
//             LocalDateTime.now();
            
//             List<Movimiento_Caja> movimientos = movimientoCajaRepository.findByFechaBetween(inicio, fin);
//         Map<String, Double> resumen = movimientoCajaService.getResumenMovimientos(inicio, fin);
        
//         model.addAttribute("movimientos", movimientos);
//         model.addAttribute("resumen", resumen);
//         //model.addAttribute("saldoActual", movimientoCajaService.getSaldoActual());
//         model.addAttribute("fechaInicio", inicio.toLocalDate());
//         model.addAttribute("fechaFin", fin.toLocalDate());
        
//         return "caja/movimientos";
//     }
    
//     //de Map<String, object> a Map<String, Double> ya que se sirve del IMovimientoCajaService
//     @GetMapping("/reporte")
//     @ResponseBody
//     public Map<String, Double> getReporteCaja(@RequestParam String fechaInicio,
//                                             @RequestParam String fechaFin) {
        
//         LocalDateTime inicio = LocalDateTime.parse(fechaInicio + "T00:00:00");
//         LocalDateTime fin = LocalDateTime.parse(fechaFin + "T23:59:59");
        
//         return movimientoCajaService.getResumenMovimientos(inicio, fin);
//     }
    

// }
