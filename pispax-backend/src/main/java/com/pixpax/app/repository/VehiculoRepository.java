package com.pixpax.app.repository;

// Repositorio de vehículos. Da acceso a la tabla "vehiculo" de MySQL.

import com.pixpax.app.entity.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {
    // Lista de vehículos que pertenecen a un transportista (para la pantalla "Mis vehículos")
    List<Vehiculo> findByTransportistaId(Long transportistaId);

    // Comprueba si ya hay un vehículo con esa matrícula (para evitar duplicados)
    boolean existsByMatricula(String matricula);
}
