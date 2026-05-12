package com.pixpax.app.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class CrearViajeRequest {

    @NotNull(message = "El tipo de mercancía es obligatorio")
    private Long tipoMercanciaId;

    @Size(max = 500)
    private String descripcionMercancia;

    @NotBlank(message = "La dirección de recogida es obligatoria")
    @Size(max = 300)
    private String direccionRecogida;

    @NotBlank(message = "La dirección de entrega es obligatoria")
    @Size(max = 300)
    private String direccionEntrega;

    @NotNull(message = "El peso es obligatorio")
    @Positive(message = "El peso debe ser mayor que 0")
    private BigDecimal pesoKg;
}
