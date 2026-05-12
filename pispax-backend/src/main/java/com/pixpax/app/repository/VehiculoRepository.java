package com.pixpax.app.repository;

import com.pixpax.app.entity.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {
    List<Vehiculo> findByTransportistaId(Long transportistaId);
    boolean existsByMatricula(String matricula);
}
