package com.analistas.luzclaritaweb.web.controller;

import com.analistas.luzclaritaweb.model.domain.Curso;
import com.analistas.luzclaritaweb.model.service.interfaces.ICursoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/cursos")
@SessionAttributes("curso")
public class CursoController {

    @Autowired
    private ICursoService cursoService;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CursoController.class);

    @GetMapping({"", "/", "/listado"})
    public String list(Model model, RedirectAttributes flash) {
        try {
            model.addAttribute("titulo", "Listado de Cursos");
            model.addAttribute("cursos", cursoService.findAll());
            return "cursos/list";
        } catch (Exception e) {
            logger.error("Error al listar cursos: {}", e.getMessage());
            flash.addFlashAttribute("error", "Error al cargar el listado de cursos");
            return "redirect:/";
        }
    }

    @GetMapping("/listado2")
    public String list2(Model model, RedirectAttributes flash) {
        try {
            model.addAttribute("titulo", "Listado de Cursos");
            model.addAttribute("cursos", cursoService.findAll());
            return "cursos/list2";
        } catch (Exception e) {
            logger.error("Error al listar cursos: {}", e.getMessage());
            flash.addFlashAttribute("error", "Error al cargar el listado de cursos");
            return "redirect:/";
        }
    }


    @GetMapping("/form")
    public String crear(Model model) {
        model.addAttribute("titulo", "Nuevo Curso");
        model.addAttribute("curso", new Curso());
        return "cursos/form";
    }

    @GetMapping("/form/{id}")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes flash) {
        try {
            Curso curso = cursoService.findById(id);
            if (curso == null) {
                flash.addFlashAttribute("error", "El curso no existe");
                return "redirect:/cursos/listado";
            }
            model.addAttribute("titulo", "Editar Curso");
            model.addAttribute("curso", curso);
            return "cursos/form";
        } catch (Exception e) {
            logger.error("Error al cargar curso para edición ID {}: {}", id, e.getMessage());
            flash.addFlashAttribute("error", "Error al cargar el curso para edición");
            return "redirect:/cursos/listado";
        }
    }

    @PostMapping("/save")
    public String guardar(@Valid @ModelAttribute("curso") Curso curso, 
                        BindingResult result,
                        Model model,
                        RedirectAttributes flash, 
                        SessionStatus status) {

        // Validación de errores del formulario
        if(result.hasErrors()) {
            logger.error("Errores de validación al guardar curso: {}", result.getAllErrors());
            model.addAttribute("titulo", curso.getId() == null ? "Nuevo Curso" : "Editar Curso");
            return "cursos/form";
        }

        try {
            // Validación específica para el video
            if (curso.getVideoUrl() != null && !curso.getVideoUrl().isEmpty()) {
                String videoUrl = curso.getVideoUrl().trim();
                if (!esUrlYoutubeValida(videoUrl)) {
                    model.addAttribute("error", "La URL de YouTube no es válida");
                    model.addAttribute("titulo", curso.getId() == null ? "Nuevo Curso" : "Editar Curso");
                    return "cursos/form";
                }
            }

            cursoService.save(curso);
            logger.info("Curso {} guardado exitosamente con ID: {}", 
                    curso.getId() == null ? "nuevo" : "existente", curso.getId());
            
            flash.addFlashAttribute("success", 
                curso.getId() == null ? "Curso creado con éxito!" : "Curso actualizado con éxito!");
            status.setComplete();
            
            return "redirect:/cursos/listado";

        } catch (DataIntegrityViolationException e) {
            String errorMsg = "Error de integridad de datos: " + e.getMostSpecificCause().getMessage();
            logger.error("Error al guardar curso (integridad): {}", errorMsg);
            model.addAttribute("error", errorMsg);
            return "cursos/form";

        } catch (IllegalArgumentException e) {
            logger.error("Error de validación al guardar curso: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("titulo", curso.getId() == null ? "Nuevo Curso" : "Editar Curso");
            return "cursos/form";

        } catch (Exception e) {
            logger.error("Error inesperado al guardar curso: {}", e.getMessage());
            flash.addFlashAttribute("error", 
                "Ocurrió un error inesperado. Por favor intente nuevamente.");
            return curso.getId() == null ? "redirect:/cursos/form" : "redirect:/cursos/form/" + curso.getId();
        }
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes flash) {
        try {
            Curso curso = cursoService.findById(id);
            if (curso != null) {
                cursoService.deleteById(id);
                flash.addFlashAttribute("success", "Curso eliminado con éxito.");
                logger.info("Curso eliminado con ID: {}", id);
            } else {
                flash.addFlashAttribute("error", "El curso no existe.");
            }
            return "redirect:/cursos/listado";
        } catch (Exception e) {
            logger.error("Error al eliminar curso ID {}: {}", id, e.getMessage());
            flash.addFlashAttribute("error", "Error al eliminar el curso: " + e.getMessage());
            return "redirect:/cursos/listado";
        }
    }

    /**
     * Valida si una URL es de YouTube válida
     */
    private boolean esUrlYoutubeValida(String url) {
        // Patrones comunes de URLs de YouTube
        String regex = "^(https?://)?(www\\.)?(youtube\\.com|youtu\\.?be)/.+$";
        return url.matches(regex);
    }

    // Detalles de un curso
    @GetMapping("/{id}")
public String verCurso(@PathVariable Long id, Model model) {
    Curso curso = cursoService.findById(id);
    if (curso != null) {
        String videoId = extraerYoutubeId(curso.getVideoUrl());
        String embedUrl = (videoId != null) ? "https://www.youtube.com/embed/" + videoId : null;
        model.addAttribute("curso", curso);
        model.addAttribute("embedUrl", embedUrl);
        return "cursos/detalle";
    }
    return "redirect:/cursos";
}

    // Método auxiliar
    private String extraerYoutubeId(String url) {
    if (url == null || url.isEmpty()) return null;
    // Para URLs como: https://www.youtube.com/watch?v=dQw4w9WgXcQ
    String regex = "(?<=v=|be/|embed/)[^&#?]+";
    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
    java.util.regex.Matcher matcher = pattern.matcher(url);
    return matcher.find() ? matcher.group() : null;
}

    /**
     * Endpoint para vista previa del video (opcional, para AJAX)
     */
    @GetMapping("/preview-video")
    @ResponseBody
    public String previewVideo(@RequestParam String url, Model model) {
        if (url == null || url.isEmpty()) {
            return "";
        }
        
        try {
            String videoId = cursoService.extractYoutubeId(url);
            if (videoId != null) {
                return "<div class='ratio ratio-16x9'><iframe src='https://www.youtube.com/embed/" + 
                       videoId + "' allowfullscreen></iframe></div>";
            }
        } catch (Exception e) {
            logger.error("Error al generar vista previa del video: {}", e.getMessage());
        }
        
        return "<div class='alert alert-warning'>URL de YouTube no válida</div>";
    }
}