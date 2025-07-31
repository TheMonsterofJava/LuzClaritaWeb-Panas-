package com.analistas.luzclaritaweb.model.service.interfaces;

import java.util.List;

import com.analistas.luzclaritaweb.model.domain.Curso;

public interface ICursoService {
    List<Curso> findAll();
    Curso findById(Long id);
    void save(Curso curso);
    void deleteById(Long id);
    
    // Nuevo método opcional para extraer el ID de YouTube
    default String extractYoutubeId(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }
        
        // Expresión regular para manejar diferentes formatos de URLs de YouTube
        String regex = "(?<=watch\\?v=|/videos/|embed\\/|youtu\\.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\\?video_id=)([^#\\&\\?\\n]*)[^\\w\\-\\s]";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(url);
        
        return matcher.find() ? matcher.group() : null;
    }
}