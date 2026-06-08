package com.pispax.app.model.request;

// Cuerpo de la peticion POST /vehiculos.
// Contiene todos los datos del formulario de CrearVehiculoActivity.
// Solo puede llamarlo un usuario con rol TRANSPORTISTA.
public class CrearVehiculoRequest {
    private String matricula;
    private String marca;
    private String modelo;
    private String tipoVehiculo;   // Enum TipoVehiculo del backend (ej: "FURGONETA", "CAMION_PESADO")
    private String subtipo;        // Texto libre opcional (ej: "frigorifico", "lona")
    private Double taraKg;         // Peso vacio en kg, opcional
    private Double capacidadKg;    // Carga maxima en kg, obligatorio
    private Double mmaKg;          // Masa Maxima Autorizada en kg, opcional
    private String carnetRequerido; // Enum CarnetRequerido del backend (ej: "B", "C", "C_E")

    public CrearVehiculoRequest(String matricula, String marca, String modelo,
                                String tipoVehiculo, String subtipo,
                                Double taraKg, Double capacidadKg,
                                Double mmaKg, String carnetRequerido) {
        this.matricula       = matricula;
        this.marca           = marca;
        this.modelo          = modelo;
        this.tipoVehiculo    = tipoVehiculo;
        this.subtipo         = subtipo;
        this.taraKg          = taraKg;
        this.capacidadKg     = capacidadKg;
        this.mmaKg           = mmaKg;
        this.carnetRequerido = carnetRequerido;
    }
}
