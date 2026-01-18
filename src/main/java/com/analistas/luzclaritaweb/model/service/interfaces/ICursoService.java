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
        
        String pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%2F|watch\\?v=|v\\/|e\\/|u\\/\\w+\\/|embed\\?video_id=)([^#\\&\\?]*).*";
        
        java.util.regex.Pattern compiledPattern = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher matcher = compiledPattern.matcher(url);
        
        if (matcher.find()){
            return matcher.group(1);
        }
        return null;
    }
}