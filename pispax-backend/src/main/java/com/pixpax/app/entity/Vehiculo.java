package com.pixpax.app.entity;

// Representa un vehículo registrado por un transportista.
// Cada transportista puede tener varios vehículos. Mapea con la tabla "vehiculo" en MySQL.

import com.pixpax.app.enums.CarnetRequerido;
import com.pixpax.app.enums.TipoVehiculo;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "vehiculo")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Vehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 15)
    private String matricula; // única en toda la tabla, no puede haber dos vehículos con la misma

    @Column(nullable = false, length = 80)
    private String marca;

    @Column(nullable = false, length = 80)
    private String modelo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_vehiculo", nullable = false)
    private TipoVehiculo tipoVehiculo;

    @Column(length = 50)
    private String subtipo; // descripción libre del tipo de vehículo (opcional)

    @Column(name = "tara_kg", precision = 8, scale = 2)
    private BigDecimal taraKg; // peso del vehículo vacío (opcional)

    @Column(name = "capacidad_kg", nullable = false, precision = 8, scale = 2)
    private BigDecimal capacidadKg; // lo máximo que puede cargar, se usa para validar si cabe la mercancía

    @Column(name = "mma_kg", precision = 8, scale = 2)
    private BigDecimal mmaKg; // Masa Máxima Autorizada (tara + carga), opcional

    @Enumerated(EnumType.STRING)
    @Column(name = "carnet_requerido", nullable = false)
    private CarnetRequerido carnetRequerido;

    // El transportista al que pertenece este vehículo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transportista_id", nullable = false)
    private Usuario transportista;

    // Lista de tipos de mercancía que puede transportar este vehículo (relación N:M)
    @OneToMany(mappedBy = "vehiculo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VehiculoTipoMercancia> compatibilidades;
}
