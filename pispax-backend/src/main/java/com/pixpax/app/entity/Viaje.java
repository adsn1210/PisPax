package com.pixpax.app.entity;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transportista_id")
    private Usuario transportista;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehiculo_id")
    private Vehiculo vehiculo;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoViaje estado = EstadoViaje.PENDIENTE;

    @CreationTimestamp
    @Column(name = "fecha_solicitud", updatable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;
}
