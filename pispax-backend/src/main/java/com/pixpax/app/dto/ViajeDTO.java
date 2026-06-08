package com.pixpax.app.dto;

// DTO del viaje: lo que se envía al cliente Android cuando pide datos de un viaje.
// En vez de devolver las entidades JPA completas (con relaciones cargadas),
// aquí solo se ponen los campos necesarios en formato plano (IDs, nombres, etc.).
// Esto evita bucles de serialización y controla exactamente qué información sale.

import com.pixpax.app.entity.Viaje;
import com.pixpax.app.enums.EstadoViaje;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ViajeDTO {
    private Long id;
    private Long clienteId;
    private String clienteNombre;
    private Long transportistaId;
    private String transportistaNombre;
    private Long vehiculoId;
    private String vehiculoMatricula;
    private Long tipoMercanciaId;
    private String tipoMercanciaNombre;
    private String descripcionMercancia;
    private String direccionRecogida;
    private String direccionEntrega;
    private BigDecimal pesoKg;
    private EstadoViaje estado;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;

    public ViajeDTO(Viaje viaje) {
        this.id = viaje.getId();
        this.clienteId = viaje.getCliente().getId();
        this.clienteNombre = viaje.getCliente().getNombre() + " " + viaje.getCliente().getApellidos();
        if (viaje.getTransportista() != null) {
            this.transportistaId = viaje.getTransportista().getId();
            this.transportistaNombre = viaje.getTransportista().getNombre() + " " + viaje.getTransportista().getApellidos();
        }
        if (viaje.getVehiculo() != null) {
            this.vehiculoId = viaje.getVehiculo().getId();
            this.vehiculoMatricula = viaje.getVehiculo().getMatricula();
        }
        if (viaje.getTipoMercancia() != null) {
            this.tipoMercanciaId = viaje.getTipoMercancia().getId();
            this.tipoMercanciaNombre = viaje.getTipoMercancia().getNombre();
        }
        this.descripcionMercancia = viaje.getDescripcionMercancia();
        this.direccionRecogida = viaje.getDireccionRecogida();
        this.direccionEntrega = viaje.getDireccionEntrega();
        this.pesoKg = viaje.getPesoKg();
        this.estado = viaje.getEstado();
        this.fechaSolicitud = viaje.getFechaSolicitud();
        this.fechaInicio = viaje.getFechaInicio();
        this.fechaFin = viaje.getFechaFin();
    }
}
