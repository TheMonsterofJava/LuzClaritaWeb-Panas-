package com.analistas.luzclaritaweb.model.service;

import com.analistas.luzclaritaweb.model.domain.Curso;
import com.analistas.luzclaritaweb.model.repository.ICursoRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CursoServiceImpl implements ICursoService {

    @Autowired
    private ICursoRepository cursoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Curso> findAll() {
        return cursoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Curso findById(Long id) {
        return cursoRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void save(Curso curso) {
        // Normalizar la URL de YouTube antes de guardar
        if (curso.getVideoUrl() != null && !curso.getVideoUrl().isEmpty()) {
            String videoId = extractYoutubeId(curso.getVideoUrl());
            if (videoId != null) {
                // Guarda la URL normalizada
                curso.setVideoUrl("https://www.youtube.com/watch?v=" + videoId);
            } else {
                // Si la URL no es válida, limpia el campo
                curso.setVideoUrl(null);
            }
        }
        cursoRepository.save(curso);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        cursoRepository.deleteById(id);
    }

}
