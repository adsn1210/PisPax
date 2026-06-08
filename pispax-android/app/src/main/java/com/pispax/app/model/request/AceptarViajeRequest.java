package com.pispax.app.model.request;

// Cuerpo de la peticion POST /viajes/{id}/aceptar.
// El transportista envia el ID del vehiculo con el que acepta el viaje.
public class AceptarViajeRequest {
    private Long vehiculoId;

    public AceptarViajeRequest(Long vehiculoId) {
        this.vehiculoId = vehiculoId;
    }
}
