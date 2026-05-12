package com.pixpax.app.entity;

import com.pixpax.app.enums.CompatibilidadMercancia;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vehiculo_tipo_mercancia")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VehiculoTipoMercancia {

    @EmbeddedId
    private VehiculoTipoMercanciaId id;

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
    private CompatibilidadMercancia compatible;
}
