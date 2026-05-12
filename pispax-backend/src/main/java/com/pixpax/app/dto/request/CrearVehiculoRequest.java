package com.pixpax.app.dto.request;

import com.pixpax.app.enums.CarnetRequerido;
import com.pixpax.app.enums.TipoVehiculo;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class CrearVehiculoRequest {

    @NotBlank(message = "La matrícula es obligatoria")
    @Size(max = 15)
    private String matricula;

    @NotBlank(message = "La marca es obligatoria")
    @Size(max = 80)
    private String marca;

    @NotBlank(message = "El modelo es obligatorio")
    @Size(max = 80)
    private String modelo;

    @NotNull(message = "El tipo de vehículo es obligatorio")
    private TipoVehiculo tipoVehiculo;

    @Size(max = 50)
    private String subtipo;

    @Positive
    private BigDecimal taraKg;

    @NotNull(message = "La capacidad es obligatoria")
    @Positive(message = "La capacidad debe ser mayor que 0")
    private BigDecimal capacidadKg;

    @Positive
    private BigDecimal mmaKg;

    @NotNull(message = "El carnet requerido es obligatorio")
    private CarnetRequerido carnetRequerido;
}
