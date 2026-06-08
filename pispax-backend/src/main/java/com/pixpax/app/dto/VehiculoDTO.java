package com.pixpax.app.dto;

// DTO del vehículo: datos del vehículo que se devuelven al cliente Android.
// Se construye a partir de la entidad Vehiculo para no exponer la entidad JPA directamente.

import com.pixpax.app.entity.Vehiculo;
import com.pixpax.app.enums.CarnetRequerido;
import com.pixpax.app.enums.TipoVehiculo;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class VehiculoDTO {
    private Long id;
    private String matricula;
    private String marca;
    private String modelo;
    private TipoVehiculo tipoVehiculo;
    private String subtipo;
    private BigDecimal taraKg;
    private BigDecimal capacidadKg;
    private BigDecimal mmaKg;
    private CarnetRequerido carnetRequerido;
    private Long transportistaId;

    public VehiculoDTO(Vehiculo vehiculo) {
        this.id = vehiculo.getId();
        this.matricula = vehiculo.getMatricula();
        this.marca = vehiculo.getMarca();
        this.modelo = vehiculo.getModelo();
        this.tipoVehiculo = vehiculo.getTipoVehiculo();
        this.subtipo = vehiculo.getSubtipo();
        this.taraKg = vehiculo.getTaraKg();
        this.capacidadKg = vehiculo.getCapacidadKg();
        this.mmaKg = vehiculo.getMmaKg();
        this.carnetRequerido = vehiculo.getCarnetRequerido();
        this.transportistaId = vehiculo.getTransportista().getId();
    }
}
