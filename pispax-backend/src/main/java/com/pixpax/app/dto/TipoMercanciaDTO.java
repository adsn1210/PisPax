package com.pixpax.app.dto;

// DTO del catálogo de mercancías. Se usa en la pantalla donde el cliente
// elige qué tipo de mercancía quiere transportar al crear un nuevo viaje.

import com.pixpax.app.entity.TipoMercancia;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TipoMercanciaDTO {

    private Long id;
    private String nombre;
    private String descripcion;

    public TipoMercanciaDTO(TipoMercancia tipoMercancia) {
        this.id = tipoMercancia.getId();
        this.nombre = tipoMercancia.getNombre();
        this.descripcion = tipoMercancia.getDescripcion();
    }
}
