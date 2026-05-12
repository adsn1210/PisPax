package com.pixpax.app.dto.request;

import com.pixpax.app.enums.EstadoViaje;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ActualizarEstadoRequest {

    @NotNull(message = "El nuevo estado es obligatorio")
    private EstadoViaje nuevoEstado;
}
