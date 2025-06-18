package com.analistas.luzclaritaweb.model.repository;

import com.analistas.luzclaritaweb.model.domain.Curso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ICursoRepository extends JpaRepository<Curso, Long> {

    // Consulta JPQL para cursos con video
    @Query("SELECT c FROM Curso c WHERE c.videoUrl IS NOT NULL AND c.videoUrl <> ''")
    List<Curso> findCursosConVideo();
    
    // Consulta derivada del método (alternativa)
    List<Curso> findByVideoUrlIsNotNullAndVideoUrlNot(String empty);
    
    // Consulta para buscar por nombre (opcional)
    List<Curso> findByNombreCursoContainingIgnoreCase(String termino);
    
    // Consulta para verificar existencia por nombre (opcional)
    boolean existsByNombreCursoIgnoreCase(String nombreCurso);

}