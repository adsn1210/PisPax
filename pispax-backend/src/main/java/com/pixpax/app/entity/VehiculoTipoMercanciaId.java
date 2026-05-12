package com.pixpax.app.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class VehiculoTipoMercanciaId implements Serializable {
    private Long vehiculoId;
    private Long tipoMercanciaId;
}
