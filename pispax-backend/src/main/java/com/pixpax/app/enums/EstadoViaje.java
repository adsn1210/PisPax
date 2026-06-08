package com.pixpax.app.enums;

// Todos los estados por los que pasa un viaje, de principio a fin.
// El orden es estricto: no se puede saltar ni retroceder (lo controla ViajeService).
// CANCELADO es la única excepción: se puede poner desde cualquier estado excepto COMPLETADO.
public enum EstadoViaje {
    PENDIENTE,          // El cliente creó el viaje, nadie lo ha aceptado aún
    ACEPTADO,           // Un transportista lo aceptó y asignó su vehículo
    SALIDA_RECOGIDA,    // El transportista sale hacia donde está la mercancía
    LLEGADA_RECOGIDA,   // El transportista ya llegó al punto de recogida
    MERCANCIA_RECOGIDA, // La mercancía está cargada y lista para viajar
    SALIDA_ENTREGA,     // El transportista sale hacia el destino final
    LLEGADA_ENTREGA,    // El transportista llegó al destino
    COMPLETADO,         // Entrega confirmada, viaje terminado con éxito
    CANCELADO           // El viaje se canceló antes de completarse
}
