package com.analistas.luzclaritaweb.model.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.analistas.luzclaritaweb.model.domain.Categoria;
import com.analistas.luzclaritaweb.model.repository.ICategoriaRepository; // Asegúrate de que esta clase exista y sea un repositorio adecuado

@Service
public class CategoriaServiceImpl implements ICategoriaService {

    // Inyección del repositorio
    @Autowired
    private ICategoriaRepository categoriaRepository;

    @Override
    public List<Categoria> buscarTodo() {
        return categoriaRepository.findAll();
    }

    @Override
    public Categoria buscarPorId(Long id) {
        // Busca una categoría por su ID. Si no existe, retorna null.
        Optional<Categoria> categoriaOpt = categoriaRepository.findById(id);
        return categoriaOpt.orElse(null);  // Retorna la categoría si se encuentra, o null si no existe
    }

    @Override
    public void guardar(Categoria categoria) {
        // Guarda la categoría en la base de datos (crea o actualiza)
        categoriaRepository.save(categoria);
    }

    @Override
    public void eliminar(Long id) {
        // Elimina una categoría por su ID
        categoriaRepository.deleteById(id);
    }
}
