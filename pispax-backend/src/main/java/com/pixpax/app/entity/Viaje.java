package com.pixpax.app.entity;

// Entidad central de la aplicación: representa una solicitud de transporte.
// Un cliente la crea con origen, destino y mercancía. El transportista la acepta y actualiza su estado.
// Mapea con la tabla "viaje" en MySQL.

import com.pixpax.app.enums.EstadoViaje;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "viaje")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Viaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // El cliente que creó este viaje (columna cliente_id en la tabla)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    // El transportista que aceptó el viaje. Está vacío (null) hasta que alguien lo acepta
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transportista_id")
    private Usuario transportista;

    // El vehículo asignado al viaje. También vacío hasta que un transportista lo acepta
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehiculo_id")
    private Vehiculo vehiculo;

    // Tipo de mercancía del catálogo (paquetería, electrónica, alimentos, etc.)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_mercancia_id")
    private TipoMercancia tipoMercancia;

    @Column(name = "descripcion_mercancia", length = 500)
    private String descripcionMercancia;

    @Column(name = "direccion_recogida", nullable = false, length = 300)
    private String direccionRecogida;

    @Column(name = "direccion_entrega", nullable = false, length = 300)
    private String direccionEntrega;

    @Column(name = "peso_kg", nullable = false, precision = 8, scale = 2)
    private BigDecimal pesoKg;

    // Estado actual del viaje. Por defecto empieza en PENDIENTE al crearse
    // @Builder.Default es necesario para que el Builder de Lombok respete el valor inicial
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoViaje estado = EstadoViaje.PENDIENTE;

    @CreationTimestamp  // se rellena automáticamente con la fecha y hora del momento de creación
    @Column(name = "fecha_solicitud", updatable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio; // se rellena cuando el transportista acepta el viaje

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin; // se rellena cuando el viaje llega a COMPLETADO o CANCELADO
}
