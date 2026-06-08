package com.pixpax.app.dto.request;

// JSON que manda el transportista para avanzar el estado de un viaje.
// El servicio valida que la transición sea válida (no se puede saltar ni retroceder).

import com.pixpax.app.enums.EstadoViaje;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ActualizarEstadoRequest {

    @NotNull(message = "El nuevo estado es obligatorio")
    private EstadoViaje nuevoEstado; // ej: "SALIDA_RECOGIDA", "COMPLETADO", etc.
}
