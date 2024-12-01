/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.analistas.luzclaritaweb.model.service;

import com.analistas.luzclaritaweb.model.domain.Consulta;
import org.springframework.stereotype.Service;

/**
 *
 * @author osval
 */
@Service
public interface IConsultaService {
    
    public void guardarConsulta(Consulta consulta);
    
}
