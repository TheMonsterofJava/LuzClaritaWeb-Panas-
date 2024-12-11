package com.analistas.luzclaritaweb.model.service;

import com.analistas.luzclaritaweb.model.domain.Consulta;
import org.springframework.stereotype.Service;

@Service
public interface IConsultaService {
    
    public void guardarConsulta(Consulta consulta);
    
}


