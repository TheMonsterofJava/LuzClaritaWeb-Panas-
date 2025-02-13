/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.analistas.luzclaritaweb.model.service;

import com.analistas.luzclaritaweb.model.domain.Consulta;
import com.analistas.luzclaritaweb.model.repository.IConsultaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
public class ConsultaServiceImpl implements IConsultaService{
    
    @Autowired
    private IConsultaRepository consultaRepository;
    
 
    @Override
    @Transactional
    public void guardarConsulta(Consulta consulta) {
    
        consultaRepository.save(consulta);
        
    }
    
}
