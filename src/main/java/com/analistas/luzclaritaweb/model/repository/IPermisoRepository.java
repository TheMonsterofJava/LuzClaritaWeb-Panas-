package com.analistas.luzclaritaweb.model.repository;
import com.analistas.luzclaritaweb.model.domain.Permiso;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IPermisoRepository extends JpaRepository<Permiso, Long> {

    Optional<Permiso> findByNombre(String nombre);

}
