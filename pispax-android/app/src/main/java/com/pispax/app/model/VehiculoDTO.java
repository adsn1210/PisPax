package com.pispax.app.model;

// Datos de un vehiculo que devuelve la API.
// Usado en MisVehiculosActivity (lista), VehiculosAdapter (items) y
// en el dialogo de aceptar viaje para seleccionar con que vehiculo se acepta.
public class VehiculoDTO {

    // Identificador unico en base de datos
    private Long id;

    // Matricula del vehiculo (ej: "1234ABC")
    private String matricula;

    // Marca del vehiculo (ej: "Mercedes")
    private String marca;

    // Modelo del vehiculo (ej: "Sprinter")
    private String modelo;

    // Tipo principal: FURGONETA, CAMION_LIGERO, CAMION_PESADO, etc.
    private String tipoVehiculo;

    // Subtipo opcional para especificar mas (ej: "frigorifico", "lona")
    private String subtipo;

    // Tara en kg (peso del vehiculo vacio), campo opcional
    private Double taraKg;

    // Capacidad maxima de carga en kg (campo obligatorio)
    private Double capacidadKg;

    // Masa Maxima Autorizada en kg, campo opcional
    private Double mmaKg;

    // Carnet necesario para conducirlo: B, C, C1, CE, C_E, etc.
    private String carnetRequerido;

    // ID del transportista propietario del vehiculo
    private Long transportistaId;

    public Long getId()               { return id; }
    public String getMatricula()      { return matricula; }
    public String getMarca()          { return marca; }
    public String getModelo()         { return modelo; }
    public String getTipoVehiculo()   { return tipoVehiculo; }
    public String getSubtipo()        { return subtipo; }
    public Double getTaraKg()         { return taraKg; }
    public Double getCapacidadKg()    { return capacidadKg; }
    public Double getMmaKg()          { return mmaKg; }
    public String getCarnetRequerido(){ return carnetRequerido; }
    public Long getTransportistaId()  { return transportistaId; }

    // Texto corto para mostrar en listas: "Mercedes Sprinter · 1234ABC"
    public String getDescripcionCorta() {
        return marca + " " + modelo + " · " + matricula;
    }
}
