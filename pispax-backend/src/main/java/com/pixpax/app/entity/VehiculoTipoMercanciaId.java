package com.pixpax.app.entity;

// Clase que representa la clave primaria compuesta de VehiculoTipoMercancia.
// JPA exige que las claves compuestas sean una clase aparte que implemente Serializable.
// @Embeddable = puede incrustarse dentro de otra entidad con @EmbeddedId.
// @EqualsAndHashCode = Lombok genera equals() y hashCode() necesarios para que JPA
// pueda comparar claves primarias correctamente.

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class VehiculoTipoMercanciaId implements Serializable {
    private Long vehiculoId;      // FK hacia la tabla vehiculo
    private Long tipoMercanciaId; // FK hacia la tabla tipo_mercancia
}
