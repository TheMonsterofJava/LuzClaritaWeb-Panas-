package com.analistas.luzclaritaweb.model.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Entity
@Table(name = "persistent_logins")
public class PersistentLogins {

    @Column(name = "username", length = 64, nullable = false)
    @NotEmpty(message = "El Nombre de Usuario es requerido...")
    @Size(min = 1, max = 64, message = "El Nombre de Usuario debe tener entre 1 y 64 caracteres...")
    private String username;

    @Id
    @Column(name = "series", length = 64, nullable = false)
    private String series;

    @Column(name = "token", length = 64, nullable = false)
    @NotEmpty(message = "El token es requerido...")
    @Size(min = 1, max = 64, message = "El token debe tener entre 1 y 64 caracteres...")
    private String token;

    @Column(name = "last_used", columnDefinition = "TIMESTAMP", nullable = false)
    private LocalDateTime lastUsed;

}