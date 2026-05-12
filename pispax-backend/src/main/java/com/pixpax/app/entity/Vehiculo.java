package com.pixpax.app.entity;

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
    private String matricula;

    @Column(nullable = false, length = 80)
    private String marca;

    @Column(nullable = false, length = 80)
    private String modelo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_vehiculo", nullable = false)
    private TipoVehiculo tipoVehiculo;

    @Column(length = 50)
    private String subtipo;

    @Column(name = "tara_kg", precision = 8, scale = 2)
    private BigDecimal taraKg;

    @Column(name = "capacidad_kg", nullable = false, precision = 8, scale = 2)
    private BigDecimal capacidadKg;

    @Column(name = "mma_kg", precision = 8, scale = 2)
    private BigDecimal mmaKg;

    @Enumerated(EnumType.STRING)
    @Column(name = "carnet_requerido", nullable = false)
    private CarnetRequerido carnetRequerido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transportista_id", nullable = false)
    private Usuario transportista;

    @OneToMany(mappedBy = "vehiculo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VehiculoTipoMercancia> compatibilidades;
}
