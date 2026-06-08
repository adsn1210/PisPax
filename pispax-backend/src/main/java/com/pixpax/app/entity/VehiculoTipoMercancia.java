package com.pixpax.app.entity;

// Tabla intermedia de la relación muchos a muchos entre Vehiculo y TipoMercancia.
// Guarda qué tipos de mercancía puede transportar cada vehículo, y con qué condiciones.
//
// Como la clave primaria es compuesta (vehiculo_id + tipo_mercancia_id), JPA necesita
// una clase separada para representarla: VehiculoTipoMercanciaId con @EmbeddedId.

import com.pixpax.app.enums.CompatibilidadMercancia;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vehiculo_tipo_mercancia")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VehiculoTipoMercancia {

    // Clave primaria compuesta: la combinación de vehiculo_id + tipo_mercancia_id es única
    @EmbeddedId
    private VehiculoTipoMercanciaId id;

    // @MapsId indica que vehiculo.id es parte de la clave primaria compuesta
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("vehiculoId")
    @JoinColumn(name = "vehiculo_id")
    private Vehiculo vehiculo;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tipoMercanciaId")
    @JoinColumn(name = "tipo_mercancia_id")
    private TipoMercancia tipoMercancia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompatibilidadMercancia compatible; // SI o CON_REQUISITOS
}
