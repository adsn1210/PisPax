package com.pixpax.app.repository;

// Repositorio de viajes. Spring genera el SQL automáticamente por el nombre del método.
// JpaRepository ya incluye save(), findById(), findAll(), delete()... sin código extra.

import com.pixpax.app.entity.Viaje;
import com.pixpax.app.enums.EstadoViaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ViajeRepository extends JpaRepository<Viaje, Long> {

    // Todos los viajes de un cliente concreto (para la pantalla "Mis viajes" del cliente)
    List<Viaje> findByClienteId(Long clienteId);

    // Todos los viajes asignados a un transportista concreto
    List<Viaje> findByTransportistaId(Long transportistaId);

    // Todos los viajes en un estado determinado (ej: PENDIENTE = viajes disponibles para aceptar)
    List<Viaje> findByEstado(EstadoViaje estado);
}
