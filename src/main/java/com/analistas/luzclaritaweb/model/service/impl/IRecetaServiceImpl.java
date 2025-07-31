package com.analistas.luzclaritaweb.model.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.analistas.luzclaritaweb.model.domain.Receta;
import com.analistas.luzclaritaweb.model.repository.IRecetaRepository;
import com.analistas.luzclaritaweb.model.service.interfaces.IRecetasService;

@Service
public class IRecetaServiceImpl implements IRecetasService {

    @Autowired
    private IRecetaRepository recetaRepository;

    @Override
    public List<Receta> buscarTodo() {
        return recetaRepository.findAll();
    }

    @Override
public Receta guardar(Receta receta) {
    return recetaRepository.save(receta);
}

    @Override
    public Receta buscarPorId(Long id) {
        return recetaRepository.findById(id).orElse(null);
    }

    @Override
    public void eliminar(Long id) {
        recetaRepository.deleteById(id);
    }

    @Override
    public List<Receta> buscarActivas() {
        return recetaRepository.findByActivoTrue();
    }
}