package com.analistas.luzclaritaweb.model.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.analistas.luzclaritaweb.model.domain.Cliente;
import com.analistas.luzclaritaweb.model.repository.IClienteRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IClienteService {

    private final IClienteRepository clienteRepository;

    public Cliente guardarCliente(Cliente cliente) {
        // La contraseña ya debe venir encriptada desde el controlador
        return clienteRepository.save(cliente);
    }

    public Optional<Cliente> findByCorreo(String correo) {
        return clienteRepository.findByCorreo(correo);
    }

    public Optional<Cliente> findById(Long Id) {
        return clienteRepository.findById(Id);
    }

    public List<Cliente> findAll() {
        return clienteRepository.findAll();
    }
}
