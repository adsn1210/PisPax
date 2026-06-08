package com.pispax.app.model.request;

// Cuerpo de la peticion PUT /viajes/{id}/estado.
// Lo usa DetalleViajeActivity para avanzar manualmente el estado de un viaje
// (solo accesible para el TRANSPORTISTA asignado a ese viaje).
public class ActualizarEstadoRequest {

    // Valor del enum EstadoViaje del backend (ej: "SALIDA_RECOGIDA", "COMPLETADO"...)
    private String nuevoEstado;

    public ActualizarEstadoRequest(String nuevoEstado) {
        this.nuevoEstado = nuevoEstado;
    }
}
