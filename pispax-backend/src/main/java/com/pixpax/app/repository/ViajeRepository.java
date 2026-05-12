package com.pixpax.app.repository;

import com.pixpax.app.entity.Viaje;
import com.pixpax.app.enums.EstadoViaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ViajeRepository extends JpaRepository<Viaje, Long> {

    List<Viaje> findByClienteId(Long clienteId);

    List<Viaje> findByTransportistaId(Long transportistaId);

    List<Viaje> findByEstado(EstadoViaje estado);

    // Viajes PENDIENTES compatibles con los vehículos del transportista
    @Query("""
        SELECT v FROM Viaje v
        WHERE v.estado = 'PENDIENTE'
          AND v.tipoMercancia.id IN (
              SELECT vtm.tipoMercancia.id
              FROM VehiculoTipoMercancia vtm
              WHERE vtm.vehiculo.transportista.id = :transportistaId
          )
        """)
    List<Viaje> findViajesDisponiblesParaTransportista(@Param("transportistaId") Long transportistaId);
}
