package com.pixpax.app.dto.request;

// El transportista manda este JSON al aceptar un viaje disponible.
// Solo necesita decir con qué vehículo lo va a hacer.
// El servicio verifica que ese vehículo le pertenece y que tiene capacidad suficiente.

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AceptarViajeRequest {

    @NotNull(message = "El vehículo es obligatorio para aceptar un viaje")
    private Long vehiculoId; // ID del vehículo con el que se acepta el viaje
}
