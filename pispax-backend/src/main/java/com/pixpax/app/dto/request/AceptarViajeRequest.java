package com.pixpax.app.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AceptarViajeRequest {

    @NotNull(message = "El vehículo es obligatorio para aceptar un viaje")
    private Long vehiculoId;
}
